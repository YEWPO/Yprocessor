package core.modules

import chisel3._
import core.CoreConfig._
import core.stages.LsuOp._
import chisel3.util.MuxLookup
import core.Common._
import chisel3.util.log2Up

class SufLsu extends Module {
  val io = IO(new Bundle {
    val addr    = Input(UInt(XLEN.W))
    val src     = Input(UInt(XLEN.W))
    val lsuOp   = Input(UInt(lsuOpLen.W))

    val data    = Output(UInt(XLEN.W))
  })

  val strbLen = log2Up(XLEN)

  val lbData = VecInit.tabulate(XLEN / byteLen) { i => io.src((i + 1) * byteLen - 1, i * byteLen) }
  val lhData = VecInit.tabulate(XLEN / halfLen) { i => io.src((i + 1) * halfLen - 1, i * halfLen) }
  val lwData = VecInit.tabulate(XLEN / wordLen) { i => io.src((i + 1) * wordLen - 1, i * wordLen) }

  io.data := MuxLookup(io.lsuOp, 0.U(XLEN.W))(Seq(
    LB    -> asSInt(lbData(io.addr(strbLen - 1, 0))).asUInt,
    LH    -> asSInt(lhData(io.addr(strbLen - 1, 1))).asUInt,
    LW    -> asSInt(lwData(io.addr(strbLen - 1, 2))).asUInt,
    LD    -> io.src,
    LBU   -> asUInt(lbData(io.addr(strbLen - 1, 0))),
    LHU   -> asUInt(lhData(io.addr(strbLen - 1, 1))),
    LWU   -> asUInt(lwData(io.addr(strbLen - 1, 2)))
  ))
}
