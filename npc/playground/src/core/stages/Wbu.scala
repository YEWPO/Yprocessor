package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._

class Wbu extends Module {
  val io = IO(new Bundle {
    val wbuIn = Flipped(Decoupled(new Bundle {
      val rd        = UInt(GPR_LEN.W)
      val lsuRes    = UInt(XLEN.W)
    }))

    val rd    = Output(UInt(GPR_LEN.W))
    val data  = Output(UInt(XLEN.W))
  })

  io.wbuIn.ready := true.B

  io.rd           := Mux(io.wbuIn.valid, io.wbuIn.bits.rd, 0.U)
  io.data         := Mux(io.wbuIn.valid, io.wbuIn.bits.lsuRes, 0.U)
}
