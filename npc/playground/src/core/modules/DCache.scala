package core.modules

import chisel3._
import core.CacheConfig._
import chisel3.util.Valid
import chisel3.util.log2Up
import core.CoreConfig._
import chisel3.util.random.LFSR
import chisel3.util.Counter
import chisel3.util.Cat
import bus.Axi4ReadAddrBundle
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.MuxCase
import chisel3.util.Fill
import chisel3.util.Decoupled
import bus.Axi4ReadDataBundle

class DCacheRequest extends Bundle {
  val addr    = UInt(ADDR_WIDTH.W)
  val data    = UInt(DATA_WIDTH.W)
  val strb    = UInt((DATA_WIDTH / 8).W)
}

class DCacheResponse extends Bundle {
  val data    = UInt(DATA_WIDTH.W)
}

object  DCacheState extends ChiselEnum {
  val sIdle, sRead, sWrite, sMiss, sRefilling, sRefilled, sHit = Value
}

class DCache extends Module {
  val io = IO(new Bundle {
    val request       = Flipped(Valid(new DCacheRequest))
    val response      = Valid(new DCacheResponse)

    val axi           = new Bundle {
      val ar = Decoupled(new Axi4ReadAddrBundle)
      val r  = Flipped(Decoupled(new Axi4ReadDataBundle))
    }
  })

  import DCacheState._
  val stateReg          = RegInit(sIdle)
  val nextState         = Wire(DCacheState())
  stateReg := nextState

  val offsetWidth       = log2Up(BLOCK_SIZE)
  val indexWidth        = log2Up(NSET)
  val tagWidth          = XLEN - indexWidth - offsetWidth
  val wordBtyes         = XLEN / 8
  val nWord             = BLOCK_SIZE / wordBtyes

  val vTable            = RegInit(VecInit(Seq.fill(NWAY)(0.U(NSET.W))))
  val tagTable          = Seq.fill(NWAY)(SyncReadMem(NSET, Vec(1, UInt(tagWidth.W))))
  val dataTable         = Seq.fill(NWAY)(SyncReadMem(NSET, Vec(nWord, UInt(XLEN.W))))

  val tag               = io.request.bits.addr(XLEN - 1, XLEN - tagWidth)
  val index             = io.request.bits.addr(indexWidth + offsetWidth - 1, offsetWidth)
  val offset            = io.request.bits.addr(offsetWidth - 1, log2Up(wordBtyes))
  val tagReg            = RegNext(tag)
  val indexReg          = RegNext(index)
  val offsetReg         = RegNext(offset)
  val randReg           = LFSR(16)
  val (readCnt, readWrap) = Counter(io.axi.r.fire, BLOCK_SIZE / wordBtyes)

  val ren               = (nextState === sRead) || (nextState === sWrite)
  val randNum           = randReg(log2Up(NWAY) - 1, 0)

  val readTag           = tagTable.map(wayTagTable => Cat(wayTagTable.read(index, ren)))
  val readData          = dataTable.map(wayDataTable => Cat(wayDataTable.read(index, ren).reverse))

  val wayHitState       = readTag.zipWithIndex.map{ case (wayTag, wayIndex) => vTable(wayIndex)(indexReg) & (wayTag === tagReg) }
  val hitData           = readData.zipWithIndex.map{ case (wayData, wayIndex) => wayData & Fill(wayData.getWidth, wayHitState(wayIndex)) }.reduce((x, y) => x | y)
  val hit               = wayHitState.reduce((x, y) => x | y)

  val refillData        = Reg(Vec(nWord, UInt(XLEN.W)))
  when (stateReg === sRefilled) {
    vTable(randNum) := vTable(randNum).bitSet(indexReg, true.B)
    tagTable.zipWithIndex.foreach{ case (wayTag, wayIndex) => wayTag.write(indexReg, VecInit(tagReg), Seq(randNum === wayIndex.U)) }
    dataTable.zipWithIndex.foreach{ case (wayDataTable, wayIndex) => wayDataTable.write(indexReg, refillData, Seq.fill(nWord)(randNum === wayIndex.U)) }
  }

  val wmask = Wire(UInt(BLOCK_SIZE.W))
  wmask := io.request.bits.strb << Cat(offsetReg, 0.U(log2Up(wordBtyes).W))
  val wdata = RegNext(
    VecInit.tabulate(BLOCK_SIZE) { i =>
      val fillData = Fill(nWord, io.request.bits.data)
      Mux(wmask(i), fillData(i * 8 + 7, i * 8), hitData(i * 8 + 7, i * 8))
    }
  ).asUInt
  val wayHitStateReg = RegNext(VecInit.tabulate(NWAY){ i => wayHitState(i) })
  when (stateReg === sHit) {
    dataTable.zipWithIndex.foreach {
      case (wayDataTable, wayIndex) =>
        wayDataTable.write(indexReg, VecInit.tabulate(nWord){ i => wdata((i + 1) * XLEN - 1, i * XLEN)}, Seq.fill(nWord)(wayHitStateReg(wayIndex)))
    }
  }

  val outData       = Mux(stateReg === sRefilled, refillData.asUInt, hitData)
  val responseValid = ((stateReg === sRead) && hit) || (stateReg === sRefilled) || (stateReg === sHit)

  io.response.bits.data := VecInit.tabulate(nWord){ i => outData((i + 1) * XLEN - 1, i * XLEN) }(offsetReg)
  io.response.valid     := responseValid

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
  switch (stateReg) {
    is (sIdle) {
      nextState := MuxCase(sIdle, Seq(
        (io.request.valid && !io.request.bits.strb.orR) -> sRead,
        (io.request.valid && io.request.bits.strb.orR)  -> sWrite
      ))
    }
    is (sRead) {
      nextState := MuxCase(sIdle, Seq(
        (hit && io.request.valid && !io.request.bits.strb.orR)  -> sRead,
        (hit && io.request.valid && io.request.bits.strb.orR)   -> sWrite,
        !hit                                                    -> sMiss
      ))
    }
    is (sWrite) {
      nextState := MuxCase(sIdle, Seq(
        (hit && io.request.valid && !io.request.bits.strb.orR)  -> sRead,
        (hit && io.request.valid && io.request.bits.strb.orR)   -> sWrite,
        hit                                                     -> sHit
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
    is (sHit) {
      nextState := sIdle
    }
  }
}
