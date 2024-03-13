package core

import chisel3._
import chisel3.util.Cat
import chisel3.util.Fill
import core.CoreConfig._

object Common {
  def asUInt[T <: Bits](bits: T): UInt = {
    val len = bits.getWidth
    val extendLen = XLEN - len

    Cat(Fill(extendLen, 0.U(1.W)), bits)
  }

  def asSInt[T <: Bits](bits: T): SInt = {
    val len = bits.getWidth
    val extendLen = XLEN - len

    Cat(Fill(extendLen, bits(len - 1)), bits).asSInt
  }

  def getWord[T <: Bits](bits: T): UInt = bits(31, 0)

  def uMax(len: Int): UInt = Fill(len, "b1".U(1.W))

  def sMax(len: Int): UInt = Cat("b0".U(1.W), uMax(len - 1))

  def sMin(len: Int): UInt = Cat("b1".U(1.W), 0.U((len - 1).W))
}
