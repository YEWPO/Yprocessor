package core.modules

import chisel3._

class Alu(xlen: Int) extends Module {
  val io = IO(new Bundle {
    val src1 = Input(UInt(xlen.W))
    val src2 = Input(UInt(xlen.W))
    val res  = Output(UInt(xlen.W))
  })

  io.res := io.src1 + io.src2
}
