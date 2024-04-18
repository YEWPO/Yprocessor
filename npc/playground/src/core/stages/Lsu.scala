package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._
import LsuOp._
import bus.Axi4Bundle
import core.modules.SufLsu

object LsuOp {
  val lsuOpLen = 5

  val byteLen = 8
  val halfLen = 2 * byteLen
  val wordLen = 2 * halfLen

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
      val lsInfo      = new Bundle {
        val wdata      = UInt(XLEN.W)
        val wstrb      = UInt((XLEN / 8).W)
      }
      val rd          = UInt(GPR_LEN.W)
      val exuRes      = UInt(XLEN.W)
      val lsuOp       = UInt(lsuOpLen.W)
      val kill        = Bool()
      val invalid     = Bool()
      val inst        = UInt(32.W)
      val dnpc        = UInt(XLEN.W)
    }))

    val lsuOut = Decoupled(new Bundle {
      val rd          = UInt(GPR_LEN.W)
      val lsuRes      = UInt(XLEN.W)
      val kill        = Bool()
      val invalid     = Bool()
      val inst        = UInt(32.W)
      val dnpc        = UInt(XLEN.W)
    })

    val axi               = new Axi4Bundle

    val rd                = Output(UInt(GPR_LEN.W))
    val data              = Output(UInt(XLEN.W))
  })

  val sufLsu              = Module(new SufLsu)


  sufLsu.io.lsuOp         := Mux(io.lsuIn.valid, io.lsuIn.bits.lsuOp, 0.U)
  sufLsu.io.addr          := io.lsuIn.bits.exuRes
  sufLsu.io.src           := 0.U // TODO

  val lsuRes              = Mux(io.lsuIn.bits.lsuOp(R_TAG), sufLsu.io.data, io.lsuIn.bits.exuRes)
  val lsuFin              = (!io.lsuIn.bits.lsuOp(R_TAG) && !io.lsuIn.bits.lsuOp(W_TAG)) // TODO

  // TODO
  // io.axi                  <> lsArbiter.io.axi

  io.rd                   := io.lsuIn.bits.rd
  io.data                 := lsuRes

  io.lsuIn.ready          := io.lsuOut.ready && lsuFin

  io.lsuOut.valid         := io.lsuIn.valid && lsuFin
  io.lsuOut.bits.rd       := io.lsuIn.bits.rd
  io.lsuOut.bits.lsuRes   := lsuRes
  io.lsuOut.bits.kill     := io.lsuIn.bits.kill
  io.lsuOut.bits.invalid  := io.lsuIn.bits.invalid
  io.lsuOut.bits.inst     := io.lsuIn.bits.inst
  io.lsuOut.bits.dnpc     := io.lsuIn.bits.dnpc
}
