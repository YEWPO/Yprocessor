package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._
import LsuOp._

object LsuOp {
  val lsuOpLen = 5

  val byteLen = 8
  val halfLen =  2 * byteLen
  val wordLen =  2 * halfLen

  val W_TAG     = 4
  val R_TAG     = 3

  val LB    = "b0_1_000".U
  val LH    = "b0_1_001".U
  val LW    = "b0_1_010".U
  val LD    = "b0_1_011".U
  val LBU   = "b0_1_100".U
  val LHU   = "b0_1_101".U
  val LWU   = "b0_1_110".U

  val SB    = "b1_0_000".U
  val SH    = "b1_0_001".U
  val SW    = "b1_0_010".U
  val SD    = "b1_0_011".U
}

class Lsu extends Module {
  val io = IO(new Bundle {
    val lsuIn = Flipped(Decoupled(new Bundle {
      val rd          = UInt(5.W)
      val exuRes      = UInt(XLEN.W)
      val lsuOp       = UInt(lsuOpLen.W)
      val kill        = Bool()
      val invalid     = Bool()
      val pc          = UInt(XLEN.W)
    }))

    val addr         = Input(UInt(XLEN.W))
    val wdata        = Input(UInt(XLEN.W))
    val wstrb        = Input(UInt((XLEN / 8).W))

    val lsuOut = Decoupled(new Bundle {
      val rd          = UInt(5.W)
      val data        = UInt(XLEN.W)
    })

    val rd            = Output(UInt(5.W))
    val data          = Output(UInt(XLEN.W))
  })

  val lsuRes          = Mux(io.lsuIn.bits.lsuOp(R_TAG), 0.U, io.lsuIn.bits.exuRes)

  io.rd               := io.lsuIn.bits.rd
  io.data             := lsuRes

  io.lsuIn.ready      := io.lsuOut.valid

  io.lsuOut.valid     := io.lsuIn.valid && false.B /** TOOD */
  io.lsuOut.bits.rd   := io.lsuIn.bits.rd
  io.lsuOut.bits.data := lsuRes
}
