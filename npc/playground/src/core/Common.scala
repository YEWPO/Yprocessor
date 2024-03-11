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
}
