package core.modules

import chisel3._
import core.modules.InstType._
import core.Common._
import chisel3.util.Cat
import chisel3.util.MuxLookup

object InstType {
  val typeWidth = 3

  val R = "b000".U
  val I = "b001".U
  val S = "b010".U
  val B = "b011".U
  val U = "b100".U
  val J = "b101".U
}

class ImmGen(xlen: Int) extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(xlen.W))
    val instType = Input(UInt(typeWidth.W))

    val imm = Output(UInt(xlen.W))
  })

  val immR = asSInt64(0.U)
  val immI = asSInt64(io.inst(31, 20))
  val immS = asSInt64(Cat(io.inst(31, 25), io.inst(11, 7)))
  val immB = asSInt64(Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8), 0.U(1.W)))
  val immU = asSInt64(Cat(io.inst(31, 12), 0.U(12.W)))
  val immJ = asSInt64(Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30, 21), 0.U(1.W)))

  io.imm := MuxLookup(io.instType, 0.U(xlen.W))(Seq(
    R -> immR.asUInt,
    I -> immI.asUInt,
    S -> immS.asUInt,
    B -> immB.asUInt,
    U -> immU.asUInt,
    J -> immJ.asUInt,
  ))
}
