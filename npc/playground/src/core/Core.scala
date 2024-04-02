package core

import chisel3._
import chisel3.util.Decoupled

class PipeReg[T <: Data](gen: T) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(gen))
    val out = Decoupled(gen)
  })

  val reg = Reg(gen)
  val valid = RegInit(false.B)

  io.in.ready := !valid
  io.out.valid := valid
  io.out.bits := reg

  when(io.in.fire) {
    reg := io.in.bits
    valid := true.B
  }

  when(io.out.fire) {
    valid := false.B
  }
}

class Core extends Module {
}
