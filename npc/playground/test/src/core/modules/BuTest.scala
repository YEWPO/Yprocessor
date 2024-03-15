package core.modules

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import core.modules.BuOp._
import scala.util.Random

case class BuInput(
  src1: Long,
  src2: Long,
  tpc: Long
)

trait BuBehavior {
  this: AnyFlatSpec =>

  def testBeq(input: BuInput): Unit = {
    val control = input.src1 == input.src2
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"beq ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(BEQ)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }

  def testBne(input: BuInput): Unit = {
    val control = input.src1 != input.src2
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"bne ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(BNE)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }

  def testJal(input: BuInput): Unit = {
    val control = true
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"jal ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(JAL)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }

  def testJalr(input: BuInput): Unit = {
    val control = true
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"jalr ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(JALR)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }

  def testBlt(input: BuInput): Unit = {
    val control = input.src1 < input.src2
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"blt ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(BLT)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }

  def testBge(input: BuInput): Unit = {
    val control = input.src1 >= input.src2
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"bge ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(BGE)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }

  def testBltu(input: BuInput): Unit = {
    val control = (input.src1 < input.src2) ^ (input.src1 < 0) ^ (input.src2 < 0)
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"bltu ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(BLTU)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }

  def testBgeu(input: BuInput): Unit = {
    val control = !((input.src1 < input.src2) ^ (input.src1 < 0) ^ (input.src2 < 0))
    val dnpc = if (control) input.tpc & ~1 else 0

    it should s"bgeu ${input.src1}, ${input.src2} -> $control, 0x${dnpc.toHexString}" in {
      simulate(new Bu) { dut =>
        dut.io.src1.poke(s"h${input.src1.toHexString}".U)
        dut.io.src2.poke(s"h${input.src2.toHexString}".U)
        dut.io.tpc.poke(s"h${input.tpc.toHexString}".U)
        dut.io.buOp.poke(BGEU)
        dut.clock.step()
        dut.io.control.expect(control.B)
        dut.io.dnpc.expect(s"h${dnpc.toHexString}".U)
      }
    }
  }
}

class BuTest extends AnyFlatSpec with BuBehavior {
  behavior of "Bu"

  val rand = new Random
  val testNum = 20

  val src1 = for (_ <- 0 until testNum) yield rand.nextLong()
  val src2 = for (_ <- 0 until testNum) yield rand.nextLong()
  val tcp  = for (_ <- 0 until testNum) yield rand.nextLong()

  for (i <- 0 until testNum) {
    testBeq(BuInput(src1(i), src2(i), tcp(i)))
    testBne(BuInput(src1(i), src2(i), tcp(i)))
    testJal(BuInput(src1(i), src2(i), tcp(i)))
    testJalr(BuInput(src1(i), src2(i), tcp(i)))
    testBlt(BuInput(src1(i), src2(i), tcp(i)))
    testBge(BuInput(src1(i), src2(i), tcp(i)))
    testBltu(BuInput(src1(i), src2(i), tcp(i)))
    testBgeu(BuInput(src1(i), src2(i), tcp(i)))
  }
}
