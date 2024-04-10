package core.modules

import chisel3._
import core.CoreConfig._
import chisel3.util.HasBlackBoxPath

class GprForward extends Module {
  val io = IO(new Bundle {
    val oriDatas    = Input(Vec(GPR_NUM, UInt(XLEN.W)))

    val rdWbu       = Input(UInt(GPR_LEN.W))
    val dataWbu     = Input(UInt(XLEN.W))
    val rdLsu       = Input(UInt(GPR_LEN.W))
    val dataLsu     = Input(UInt(XLEN.W))

    val tarDatas    = Output(Vec(GPR_NUM, UInt(XLEN.W)))
  })

  io.tarDatas           := io.oriDatas
  io.tarDatas(io.rdWbu) := io.dataWbu
  io.tarDatas(io.rdLsu) := io.dataLsu
}

class GprInfoBlackBox extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val inbits = Input(UInt((XLEN * GPR_NUM).W))
  })

  addPath("playground/src/core/modules/GprInfoBlackBox.sv")
}

class Gpr extends Module {
  val io = IO(new Bundle {
    val rdWbu     = Input(UInt(GPR_LEN.W))
    val dataWbu   = Input(UInt(XLEN.W))
    val rdLsu     = Input(UInt(GPR_LEN.W))
    val dataLsu   = Input(UInt(XLEN.W))

    val rs1       = Input(UInt(GPR_LEN.W))
    val rs2       = Input(UInt(GPR_LEN.W))

    val src1      = Output(UInt(XLEN.W))
    val src2      = Output(UInt(XLEN.W))
  })

  val gprs = RegInit(VecInit(Seq.fill(GPR_NUM)(0.U(XLEN.W))))
  gprs(io.rdWbu) := io.dataWbu

  val gprInfo = Module(new GprInfoBlackBox)
  gprInfo.io.inbits := gprs.asUInt

  val gprForward = Module(new GprForward)
  gprForward.io.oriDatas  := gprs
  gprForward.io.rdWbu     := io.rdWbu
  gprForward.io.rdLsu     := io.rdLsu
  gprForward.io.dataWbu   := io.dataWbu
  gprForward.io.dataLsu   := io.dataLsu

  val trueGpr = Wire(Vec(GPR_NUM, UInt(XLEN.W)))
  trueGpr     := gprForward.io.tarDatas
  trueGpr(0)  := 0.U(XLEN.W)

  io.src1 := trueGpr(io.rs1)
  io.src2 := trueGpr(io.rs2)
}
