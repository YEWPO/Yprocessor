package core.modules

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import core.CoreConfig._
import core.modules.InstType._

trait ImmGenBehavior {
  this: AnyFlatSpec =>

  def sext(src: Long, width: Int): Long = {
    (src << (XLEN - width)) >> (XLEN - width)
  }

  def bits(src: Int, high: Int, low: Int): Long = {
    (src.toLong >> low) & ((1L << (high - low + 1)) - 1L)
  }

  def testR(inst: Int): Unit = {
    val imm = 0

    it should s"0x${inst.toHexString} R-type immediate is 0x${imm.toHexString}" in {
      simulate(new ImmGen(XLEN)) { dut =>
        dut.io.inst.poke(s"h${inst.toHexString}".U)
        dut.io.instType.poke(R)
        dut.clock.step()
        dut.io.imm.expect(s"h${imm.toHexString}".U)
      }
    }
  }

  def testI(inst: Int): Unit = {
    val imm = sext(bits(inst, 31, 20), 12)

    it should s"0x${inst.toHexString} I-type immediate is 0x${imm.toHexString}" in {
      simulate(new ImmGen(XLEN)) { dut =>
        dut.io.inst.poke(s"h${inst.toHexString}".U)
        dut.io.instType.poke(I)
        dut.clock.step()
        dut.io.imm.expect(s"h${imm.toHexString}".U)
      }
    }
  }

  def testS(inst: Int): Unit = {
    val imm = (sext(bits(inst, 31, 25), 7) << 5) | bits(inst, 11, 7)

    it should s"0x${inst.toHexString} S-type immediate is 0x${imm.toHexString}" in {
      simulate(new ImmGen(XLEN)) { dut =>
        dut.io.inst.poke(s"h${inst.toHexString}".U)
        dut.io.instType.poke(S)
        dut.clock.step()
        dut.io.imm.expect(s"h${imm.toHexString}".U)
      }
    }
  }

  def testB(inst: Int): Unit = {
    val imm = (sext(bits(inst, 31, 31), 1) << 12) | (bits(inst, 7, 7) << 11) | (bits(inst, 30, 25) << 5) | (bits(inst, 11, 8) << 1)

    it should s"0x${inst.toHexString} B-type immediate is 0x${imm.toHexString}" in {
      simulate(new ImmGen(XLEN)) { dut =>
        dut.io.inst.poke(s"h${inst.toHexString}".U)
        dut.io.instType.poke(B)
        dut.clock.step()
        dut.io.imm.expect(s"h${imm.toHexString}".U)
      }
    }
  }

  def testU(inst: Int): Unit = {
    val imm = sext(bits(inst, 31, 12), 20) << 12

    it should s"0x${inst.toHexString} U-type immediate is 0x${imm.toHexString}" in {
      simulate(new ImmGen(XLEN)) { dut =>
        dut.io.inst.poke(s"h${inst.toHexString}".U)
        dut.io.instType.poke(U)
        dut.clock.step()
        dut.io.imm.expect(s"h${imm.toHexString}".U)
      }
    }
  }

  def testJ(inst: Int): Unit = {
    val imm = (sext(bits(inst, 31, 31), 1) << 20) | (bits(inst, 19, 12) << 12) | (bits(inst, 20, 20) << 11) | (bits(inst, 30, 21) << 1)

    it should s"0x${inst.toHexString} J-type immediate is 0x${imm.toHexString}" in {
      simulate(new ImmGen(XLEN)) { dut =>
        dut.io.inst.poke(s"h${inst.toHexString}".U)
        dut.io.instType.poke(J)
        dut.clock.step()
        dut.io.imm.expect(s"h${imm.toHexString}".U)
      }
    }
  }
}

class ImmGenTest extends AnyFlatSpec with ImmGenBehavior {
  behavior of "ImmGen"

  val testData = List[Int](0x01ca0117, 0xffc10113, 0x429000ef, 0x01213023, 0x00060e63, 0x00060913)

  testData.foreach { data =>
    testR(data)
    testI(data)
    testS(data)
    testB(data)
    testU(data)
    testJ(data)
  }
}
