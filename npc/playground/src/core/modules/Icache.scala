package core.modules

import chisel3._
import core.IcacheConfig._
import chisel3.util.Valid
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.log2Up
import core.CoreConfig._
import chisel3.util.Cat
import chisel3.util.MuxCase
import bus.Axi4Bundle
import bus.Axi4ReadAddrBundle

class IcacheRequest extends Bundle {
  val addr    = UInt(ADDR_WIDTH.W)
}

class IcacheResponse extends Bundle {
  val data    = UInt(DATA_WIDTH.W)
}

object IcacheState extends ChiselEnum {
  val sIdle, sRead, sMiss, sRefill = Value
}

class Icache extends Module {
  val io = IO(new Bundle {
    val abort         = Input(Bool())
    val request       = Flipped(Valid(new IcacheRequest))
    val response      = Valid(new IcacheResponse)
    val axi           = new Axi4Bundle
  })

  import IcacheState._
  val stateReg      = RegInit(sIdle)
  val nextState     = Wire(IcacheState())
  stateReg := nextState

  val offsetWidth   = log2Up(BLOCK_SIZE)
  val indexWidth    = log2Up(NSET)
  val tagWidth      = XLEN - indexWidth - offsetWidth
  val wordBtyes     = XLEN / 8
  val nWord         = BLOCK_SIZE / wordBtyes

  val vTable        = RegInit(VecInit(Seq.fill(NWAY)(0.U(NSET.W))))
  val tagTable      = Seq.fill(NWAY)(SyncReadMem(NSET, UInt(tagWidth.W)))
  val dataTable     = Seq.fill(NWAY, nWord)(SyncReadMem(NSET, Vec(wordBtyes, UInt(8.W))))

  val addrReg       = Reg(UInt(ADDR_WIDTH.W))

  val tag           = io.request.bits.addr(XLEN - 1, XLEN - tagWidth)
  val index         = io.request.bits.addr(indexWidth + offsetWidth - 1, offsetWidth)
  val offset        = io.request.bits.addr(offsetWidth - 1, log2Up(wordBtyes))
  val tagReg        = RegNext(tag)
  val indexReg      = RegNext(index)
  val offsetReg     = RegNext(offset)

  val ren           = nextState === sRead

  val readTag       = tagTable.map(_.read(index, ren))
  val readData      = dataTable.map(dataMem => Cat(dataMem.map(_.read(index, ren).asUInt).reverse))

  val wayHitState   = readTag.zipWithIndex.map{ case (wayTag, i) => vTable(i)(indexReg) & (wayTag === tagReg) }
  val hit           = wayHitState.reduce((x, y) => x | y)
  val hitData       = readData.zipWithIndex.map{ case (dataLine, i) => dataLine & wayHitState(i).asUInt }.reduce((x, y) => x | y)

  val refillData    = Reg(Vec(BLOCK_SIZE / wordBtyes, UInt(XLEN.W)))
  val refillFin     = RegNext((stateReg === sRefill) && (nextState =/= sRefill))

  val outData       = Mux(refillFin, refillData.asUInt, hitData)

  io.response.bits.data := VecInit.tabulate(nWord){ i => outData((i + 1) * XLEN - 1, i * XLEN) }(offsetReg)
  io.response.valid     := ((stateReg === sRead) && hit) || refillFin

  io.axi.ar.valid := stateReg === sMiss
  io.axi.ar.bits  := Axi4ReadAddrBundle(
    Cat(tagReg, indexReg) << offsetWidth,
    (BLOCK_SIZE / wordBtyes - 1).U,
    log2Up(wordBtyes).U
  )

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
      nextState := Mux(io.axi.ar.fire, sRefill, sMiss)
    }
    is (sRefill) {
      val lastData = io.axi.r.valid & io.axi.r.bits.last
      nextState := MuxCase(sIdle, Seq(
        (lastData & io.request.valid)       -> sRead,
        (lastData & !io.request.valid)      -> sIdle,
        !lastData                           -> sRefill
      ))
    }
  }
}
