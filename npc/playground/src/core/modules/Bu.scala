package core.modules

import chisel3._
import core.CoreConfig._
import BuOp._
import chisel3.util.MuxLookup
import chisel3.util.Cat

object BuOp {
  val buOpLen = 3

  val BEQ     = "b000".U
  val BNE     = "b001".U
  val JAL     = "b010".U
  val JALR    = "b011".U
  val BLT     = "b100".U
  val BGE     = "b101".U
  val BLTU    = "b110".U
  val BGEU    = "b111".U
}

class Bu extends Module {
  val io = IO(new Bundle {
    val src1      = Input(UInt(XLEN.W))
    val src2      = Input(UInt(XLEN.W))
    val tpc       = Input(UInt(XLEN.W))
    val buOp      = Input(UInt(buOpLen.W))

    val control   = Output(Bool())
    val dnpc      = Output(UInt(XLEN.W))
  })

  val eq      = io.src1         ===     io.src2
  val blt     = io.src1.asSInt  <       io.src2.asSInt
  val bltu    = io.src1         <       io.src2

  val control = MuxLookup(io.buOp, false.B)(Seq(
    BEQ     -> eq,
    BNE     -> !eq,
    JAL     -> true.B,
    JALR    -> true.B,
    BLT     -> blt,
    BGE     -> !blt,
    BLTU    -> bltu,
    BGEU    -> !bltu
  ))

  io.control := control
  io.dnpc := Mux(control, Cat(io.tpc(XLEN - 1, 1), 0.U(1.W)), 0.U(XLEN.W))
}
