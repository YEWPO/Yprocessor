package core.modules

import chisel3._
import core.CoreConfig._
import BuOp._
import chisel3.util.MuxLookup
import chisel3.util.Cat

object BuOp {
  val buOpLen = 4

  val BEQ     = "b1_000".U
  val BNE     = "b1_001".U
  val JAL     = "b1_010".U
  val JALR    = "b1_011".U
  val BLT     = "b1_100".U
  val BGE     = "b1_101".U
  val BLTU    = "b1_110".U
  val BGEU    = "b1_111".U
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
