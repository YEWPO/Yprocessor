package core.modules

import chisel3._
import core.CoreConfig._

class SnpcGen extends Module {
  val io = IO(new Bundle {
    val pc        = Input(UInt(XLEN.W))
    val instLen   = Input(UInt(2.W))

    val snpc      = Output(UInt(XLEN.W))
  })

  io.snpc := io.pc + Mux(io.instLen.andR, 4.U, 0.U)
}

class PcGen extends Module {
  val io = IO(new Bundle {
    val pc        = Input(UInt(XLEN.W))
    val instLen   = Input(UInt(2.W))
    val control   = Input(Bool())
    val dnpc      = Input(UInt(XLEN.W))

    val snpc      = Output(UInt(XLEN.W))
    val npc       = Output(UInt(XLEN.W))
  })

  val snpcGen = Module(new SnpcGen)
  snpcGen.io.pc       := io.pc
  snpcGen.io.instLen  := io.instLen

  val snpc = snpcGen.io.snpc

  io.snpc := snpc
  io.npc  := Mux(io.control, io.dnpc, snpc)
}
