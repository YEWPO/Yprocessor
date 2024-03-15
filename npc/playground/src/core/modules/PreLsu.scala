package core.modules

import chisel3._
import core.CoreConfig._
import chisel3.util.log2Up
import core.stages.LsuOp._
import chisel3.util.MuxLookup
import chisel3.util.Fill
import core.Common._

class PreLsu extends Module {
  val io = IO(new Bundle {
    val addr  = Input(UInt(XLEN.W))
    val src   = Input(UInt(XLEN.W))
    val lsuOp = Input(UInt(lsuOpLen.W))

    val data  = Output(UInt(XLEN.W))
    val strb  = Output(UInt(log2Up(XLEN).W))
  })

  val strbLen = log2Up(XLEN)

  io.data := MuxLookup(io.lsuOp, 0.U(XLEN.W))(Seq(
    SB    -> Fill(XLEN / byteLen, io.src(byteLen - 1, 0)),
    SH    -> Fill(XLEN / halfLen, io.src(halfLen - 1, 0)),
    SW    -> Fill(XLEN / wordLen, io.src(wordLen - 1, 0)),
    SD    -> Fill(XLEN / XLEN,    io.src(XLEN - 1, 0))
  ))

  val sbStrb = VecInit.tabulate(XLEN / byteLen) { i => "b1"       .U(strbLen.W) << i }
  val shStrb = VecInit.tabulate(XLEN / halfLen) { i => "b11"      .U(strbLen.W) << i }
  val swStrb = VecInit.tabulate(XLEN / wordLen) { i => "b1111"    .U(strbLen.W) << i }

  io.strb := MuxLookup(io.lsuOp, 0.U(strbLen.W))(Seq(
    SB    -> sbStrb(io.addr(log2Up(strbLen) - 1, 0)),
    SH    -> shStrb(io.addr(log2Up(strbLen) - 1, 1)),
    SW    -> swStrb(io.addr(log2Up(strbLen) - 1, 2)),
    SD    -> uMax(log2Up(strbLen))
  ))
}
