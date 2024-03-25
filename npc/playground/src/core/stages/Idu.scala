package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._
import core.modules.Decoder
import core.modules.ImmGen
import core.modules.AluOp._
import core.modules.BuOp._
import LsuOp._

class Idu extends Module {
  val io = IO(new Bundle {
    val iduIn     = Flipped(Decoupled(new Bundle {
      val inst  = UInt(32.W)
      val pc    = UInt(XLEN.W)
      val snpc  = UInt(XLEN.W)
    }))

    val iduOut    = Decoupled(new Bundle {
      val rs1         = UInt(5.W)
      val rs2         = UInt(5.W)
      val rd          = UInt(5.W)
      val imm         = UInt(XLEN.W)
      val src1Sel     = Bool()
      val src2Sel     = Bool()
      val aluOp       = UInt(aluOpLen.W)
      val buOp        = UInt(buOpLen.W)
      val lsuOp       = UInt(lsuOpLen.W)
      val kill        = Bool()
      val invalid     = Bool()
      val pc          = UInt(XLEN.W)
      val snpc        = UInt(XLEN.W)
    })

    val abort     = Input(Bool())
  })

  val decoder     = Module(new Decoder)
  val immGen      = Module(new ImmGen)

  decoder.io.inst         := io.iduIn.bits.inst

  immGen.io.inst          := io.iduIn.bits.inst
  immGen.io.instType      := decoder.io.instType

  io.iduIn.ready          := io.iduOut.ready

  io.iduOut.valid         := io.iduIn.valid && !io.abort
  io.iduOut.bits.rs1      := decoder.io.rs1
  io.iduOut.bits.rs2      := decoder.io.rs2
  io.iduOut.bits.rd       := decoder.io.rd
  io.iduOut.bits.imm      := immGen.io.imm
  io.iduOut.bits.src1Sel  := decoder.io.src1Sel
  io.iduOut.bits.src2Sel  := decoder.io.src2Sel
  io.iduOut.bits.aluOp    := decoder.io.aluOp
  io.iduOut.bits.buOp     := decoder.io.buOp
  io.iduOut.bits.lsuOp    := decoder.io.lsuOp
  io.iduOut.bits.kill     := decoder.io.kill
  io.iduOut.bits.invalid  := decoder.io.invalid
}
