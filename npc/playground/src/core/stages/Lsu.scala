package core.stages

import chisel3._

object LsuOp {
  val lsuOpLen = 5

  val byteLen = 8
  val halfLen =  2 * byteLen
  val wordLen =  2 * halfLen

  val LB    = "b0_1_000".U
  val LH    = "b0_1_001".U
  val LW    = "b0_1_010".U
  val LD    = "b0_1_011".U
  val LBU   = "b0_1_000".U
  val LHU   = "b0_1_001".U
  val LWU   = "b0_1_010".U

  val SB    = "b1_0_000".U
  val SH    = "b1_0_001".U
  val SW    = "b1_0_010".U
  val SD    = "b1_0_011".U
}

class Lsu extends Module {
}
