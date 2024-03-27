package core.modules

import chisel3._
import core.CacheConfig._
import chisel3.util.Valid
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.log2Up
import core.CoreConfig._
import chisel3.util.Cat
import chisel3.util.MuxCase
import bus.Axi4ReadAddrBundle
import chisel3.util.Counter
import chisel3.util.random.LFSR
import bus.Axi4ReadDataBundle
import chisel3.util.Decoupled
import chisel3.util.Fill

class CacheRequest extends Bundle {
  val addr    = UInt(ADDR_WIDTH.W)
}

class CacheResponse extends Bundle {
  val data    = UInt(DATA_WIDTH.W)
}

object CacheState extends ChiselEnum {
  val sIdle, sRead, sMiss, sRefilling, sRefilled = Value
}

class Cache extends Module {
  val io = IO(new Bundle {
    val abort         = Input(Bool())
    val request       = Flipped(Valid(new CacheRequest))
    val response      = Valid(new CacheResponse)
    val axi           = new Bundle {
      val ar = Decoupled(new Axi4ReadAddrBundle)
      val r  = Flipped(Decoupled(new Axi4ReadDataBundle))
    }
  })

  import CacheState._
  val stateReg      = RegInit(sIdle)
  val nextState     = Wire(CacheState())
  stateReg := nextState

  val offsetWidth   = log2Up(BLOCK_SIZE)
  val indexWidth    = log2Up(NSET)
  val tagWidth      = XLEN - indexWidth - offsetWidth
  val wordBtyes     = XLEN / 8
  val nWord         = BLOCK_SIZE / wordBtyes

  val vTable        = RegInit(VecInit(Seq.fill(NWAY)(0.U(NSET.W))))
  val tagTable      = Seq.fill(NWAY)(SyncReadMem(NSET, Vec(1, UInt(tagWidth.W))))
  val dataTable     = Seq.fill(NWAY)(SyncReadMem(NSET, Vec(nWord, UInt(XLEN.W))))

  val tag           = io.request.bits.addr(XLEN - 1, XLEN - tagWidth)
  val index         = io.request.bits.addr(indexWidth + offsetWidth - 1, offsetWidth)
  val offset        = io.request.bits.addr(offsetWidth - 1, log2Up(wordBtyes))
  val tagReg        = RegNext(tag)
  val indexReg      = RegNext(index)
  val offsetReg     = RegNext(offset)
  val randReg       = LFSR(16)
  val (readCnt, readWrap) = Counter(io.axi.r.fire, BLOCK_SIZE / wordBtyes)

  val ren           = nextState === sRead
  val randNum       = randReg(log2Up(NWAY) - 1, 0)

  val readTag       = tagTable.map(wayTagTable => Cat(wayTagTable.read(index, ren)))
  val readData      = dataTable.map(wayDataTable => Cat(wayDataTable.read(index, ren).reverse))

  val wayHitState   = readTag.zipWithIndex.map{ case (wayTag, wayIndex) => vTable(wayIndex)(indexReg) & (wayTag === tagReg) }
  val hitData       = readData.zipWithIndex.map{ case (wayData, wayIndex) => wayData & Fill(wayData.getWidth, wayHitState(wayIndex)) }.reduce((x, y) => x | y)
  val hit           = wayHitState.reduce((x, y) => x | y)

  val refillData    = Reg(Vec(BLOCK_SIZE / wordBtyes, UInt(XLEN.W)))
  when (stateReg === sRefilled) {
    vTable(randNum) := vTable(randNum).bitSet(indexReg, true.B)
    tagTable.zipWithIndex.foreach{ case (wayTag, wayIndex) => wayTag.write(indexReg, VecInit(tagReg), Seq(randNum === wayIndex.U)) }
    dataTable.zipWithIndex.foreach{ case (wayDataTable, wayIndex) => wayDataTable.write(indexReg, refillData, Seq.fill(nWord)(randNum === wayIndex.U)) }
  }

  val outData       = Mux(stateReg === sRefilled, refillData.asUInt, hitData)
  val responseValid = ((stateReg === sRead) && hit) || (stateReg === sRefilled)

  val abortReg      = Reg(Bool())
  val abort         = io.abort && (stateReg =/= sIdle)
  abortReg          := Mux(responseValid, false.B, abortReg || abort)

  io.response.bits.data := VecInit.tabulate(nWord){ i => outData((i + 1) * XLEN - 1, i * XLEN) }(offsetReg)
  io.response.valid     := responseValid && !abort && !abortReg

  io.axi.ar.valid := stateReg === sMiss
  io.axi.ar.bits  := Axi4ReadAddrBundle(
    Cat(tagReg, indexReg) << offsetWidth,
    (BLOCK_SIZE / wordBtyes - 1).U,
    log2Up(wordBtyes).U
  )

  io.axi.r.ready := stateReg === sRefilling
  when (io.axi.r.fire) {
    refillData(readCnt) := io.axi.r.bits.data
  }

  nextState := sIdle
  switch(stateReg) {
    is (sIdle) {
      nextState := Mux(io.request.valid, sRead, sIdle)
    }
    is (sRead) {
      nextState := MuxCase(sIdle, Seq(
        (hit && io.request.valid)           -> sRead,
        (hit && !io.request.valid)          -> sIdle,
        !hit                                -> sMiss
      ))
    }
    is (sMiss) {
      nextState := Mux(io.axi.ar.fire, sRefilling, sMiss)
    }
    is (sRefilling) {
      nextState := Mux(readWrap, sRefilled, sRefilling)
    }
    is (sRefilled) {
      nextState := sIdle
    }
  }
}
