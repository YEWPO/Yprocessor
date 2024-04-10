package bus

import chisel3._
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.MuxCase
import chisel3.util.Decoupled

object AxiArbiterState extends ChiselEnum {
  val IDLE, IFU, LSU = Value
}

class AxiArbiter extends Module {
  val io = IO(new Bundle {
    val ifu = new Bundle {
      val ar = Flipped(Decoupled(new Axi4ReadAddrBundle))
      val r  = Decoupled(new Axi4ReadDataBundle)
    }
    val lsu = Flipped(new Axi4Bundle)

    val axi = new Axi4Bundle
  })

  import AxiArbiterState._
  val stateReg      = RegInit(IDLE)
  val nextState     = WireDefault(IDLE)
  stateReg          := nextState

  io.axi.aw <> io.lsu.aw
  io.axi.w  <> io.lsu.w
  io.lsu.b  <> io.axi.b

  io.axi.ar.valid       := Mux(stateReg === IFU, io.ifu.ar.valid, Mux(stateReg === LSU, io.lsu.ar.valid, false.B))
  io.ifu.ar.ready       := Mux(stateReg === IFU, io.axi.ar.ready, false.B)
  io.lsu.ar.ready       := Mux(stateReg === LSU, io.axi.ar.ready, false.B)
  io.axi.ar.bits        := Mux(stateReg === IFU, io.ifu.ar.bits, io.lsu.ar.bits)

  io.ifu.r.valid        := Mux(stateReg === IFU, io.axi.r.valid, false.B)
  io.lsu.r.valid        := Mux(stateReg === LSU, io.axi.r.valid, false.B)
  io.axi.r.ready        := Mux(stateReg === IFU, io.ifu.r.ready, Mux(stateReg === LSU, io.lsu.r.ready, false.B))
  io.ifu.r.bits         := io.axi.r.bits
  io.lsu.r.bits         := io.axi.r.bits

  nextState := IDLE
  switch (stateReg) {
    is (IDLE) {
      nextState := MuxCase(IDLE, Seq(
        io.lsu.ar.valid -> LSU,
        io.ifu.ar.valid -> IFU,
      ))
    }

    is (IFU) {
      nextState := MuxCase(IFU, Seq(
        (io.ifu.r.fire && io.ifu.r.bits.last && io.lsu.ar.valid) -> LSU,
        (io.ifu.r.fire && io.ifu.r.bits.last && io.ifu.ar.valid) -> IFU,
        (io.ifu.r.fire && io.ifu.r.bits.last) -> IDLE,
      ))
    }

    is (LSU) {
      nextState := MuxCase(LSU, Seq(
        (io.lsu.r.fire && io.lsu.r.bits.last && io.lsu.ar.valid) -> LSU,
        (io.lsu.r.fire && io.lsu.r.bits.last && io.ifu.ar.valid) -> IFU,
        (io.lsu.r.fire && io.lsu.r.bits.last) -> IDLE,
      ))
    }
  }
}
