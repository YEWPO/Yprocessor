package core

import chisel3._
import chisel3.util.Cat
import chisel3.util.Fill

object Common {
  def asUInt64[T <: Bits](bits: T): UInt = {
    val len = bits.getWidth
    val extendLen = 64 - len

    Cat(Fill(extendLen, 0.U(1.W)), bits)
  }

  def asSInt64[T <: Bits](bits: T): SInt = {
    val len = bits.getWidth
    val extendLen = 64 - len

    Cat(Fill(extendLen, bits(len - 1)), bits).asSInt
  }
}
