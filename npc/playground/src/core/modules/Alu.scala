package core.modules

import chisel3._
import core.CoreConfig._
import core.modules.AluOp._

object AluOp {
  val aluOpLen = 6

  val ADD       = "b0_0_0_000".U
  val SLL       = "b0_0_0_001".U
  val SLT       = "b0_0_0_010".U
  val SLTU      = "b0_0_0_011".U
  val XOR       = "b0_0_0_100".U
  val SRL       = "b0_0_0_101".U
  val OR        = "b0_0_0_110".U
  val AND       = "b0_0_0_111".U

  val SUB       = "b0_0_1_000".U
  val SRA       = "b0_0_1_101".U

  val ADDW      = "b0_1_0_000".U
  val SUBW      = "b0_1_1_000".U
  val SLLW      = "b0_1_0_001".U
  val SRLW      = "b0_1_0_101".U
  val SRAW      = "b0_1_1_101".U

  val MUL       = "b1_0_0_000".U
  val MULH      = "b1_0_0_001".U
  val MULHSU    = "b1_0_0_010".U
  val MULHU     = "b1_0_0_011".U
  val DIV       = "b1_0_0_100".U
  val DIVU      = "b1_0_0_101".U
  val REM       = "b1_0_0_110".U
  val REMU      = "b1_0_0_111".U

  val MULW      = "b1_1_0_000".U
  val DIVW      = "b1_1_0_100".U
  val DIVUW     = "b1_1_0_101".U
  val REMW      = "b1_1_0_110".U
  val REMUW     = "b1_1_0_111".U
}

class Alu extends Module {
  val io = IO(new Bundle {
    val src1      = Input(UInt(XLEN.W))
    val src2      = Input(UInt(XLEN.W))
    val aluOp     = Input(UInt(aluOpLen.W))

    val res       = Output(UInt(XLEN.W))
  })
}
