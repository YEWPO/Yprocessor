package core.modules

import chisel3._
import core.CoreConfig._
import core.Common._
import core.modules.AluOp._
import chisel3.util.log2Up
import chisel3.util.MuxCase

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

  /** standard XLEN ops */
  val shamtWidthXlen = log2Up(XLEN)

  val divByZero = !io.src2.andR
  val overflow  = io.src1(XLEN - 1).andR & !io.src1(XLEN - 2, 0).orR & io.src2.andR

  val pMul    = io.src1.asSInt    *     io.src2.asSInt
  val pMulsu  = io.src1.asSInt    *     io.src2
  val pMulu   = io.src1           *     io.src2
  val pDiv    = io.src1.asSInt    /     io.src2.asSInt
  val pDivu   = io.src1           /     io.src2
  val pRem    = io.src1.asSInt    %     io.src2.asSInt
  val pRemu   = io.src1           %     io.src2

  val add     = io.src1           +     io.src2
  val sll     = io.src1           <<    io.src2(shamtWidthXlen - 1, 0)
  val slt     = io.src1.asSInt    <     io.src2.asSInt
  val sltu    = io.src1           <     io.src2
  val xor     = io.src1           ^     io.src2
  val srl     = io.src1           >>    io.src2(shamtWidthXlen - 1, 0)
  val or      = io.src1           |     io.src2
  val and     = io.src1           &     io.src2

  val sub     = io.src1           -     io.src2
  val sra     = io.src1.asSInt    >>    io.src2(shamtWidthXlen - 1, 0)

  val mul     = pMul(XLEN - 1, 0)
  val mulh    = pMul(2 * XLEN - 1, 2 * XLEN)
  val mulhsu  = pMulsu(2 * XLEN - 1, 2 * XLEN)
  val mulhu   = pMulu(2 * XLEN - 1, 2 * XLEN)
  val div     = MuxCase(pDiv, Seq(divByZero -> uMax(XLEN), overflow -> sMin(XLEN)))
  val divu    = Mux(divByZero, uMax(XLEN), pDivu)
  val rem     = MuxCase(pRem, Seq(divByZero -> io.src1, overflow -> 0.U(XLEN.W)))
  val remu    = Mux(divByZero, io.src1, pRemu)
}
