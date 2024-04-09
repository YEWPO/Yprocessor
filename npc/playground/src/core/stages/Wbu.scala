package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._
import chisel3.util.HasBlackBoxPath

class InstInfoBlackBox extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val inst      = Input(UInt(32.W))
    val dnpc      = Input(UInt(XLEN.W))
    val kill      = Input(Bool())
    val invalid   = Input(Bool())
    val en        = Input(Bool())
  })

  addPath("playground/src/core/modules/InstInfoBlackBox.sv")
}

class Wbu extends Module {
  val io = IO(new Bundle {
    val wbuIn = Flipped(Decoupled(new Bundle {
      val rd        = UInt(GPR_LEN.W)
      val lsuRes    = UInt(XLEN.W)
      val kill      = Bool()
      val invalid   = Bool()
      val inst      = UInt(32.W)
      val dnpc      = UInt(XLEN.W)
    }))

    val rd    = Output(UInt(GPR_LEN.W))
    val data  = Output(UInt(XLEN.W))
  })

  val instInfo = Module(new InstInfoBlackBox)
  instInfo.io.inst    := io.wbuIn.bits.inst
  instInfo.io.dnpc    := io.wbuIn.bits.dnpc
  instInfo.io.kill    := io.wbuIn.bits.kill
  instInfo.io.invalid := io.wbuIn.bits.invalid
  instInfo.io.en      := io.wbuIn.valid

  io.wbuIn.ready := true.B

  io.rd           := Mux(io.wbuIn.valid, io.wbuIn.bits.rd, 0.U)
  io.data         := Mux(io.wbuIn.valid, io.wbuIn.bits.lsuRes, 0.U)
}
