package core.modules

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import core.stages.LsuOp._
import core.CoreConfig._
import scala.util.Random

trait PreLsuBehavior {
  this: AnyFlatSpec =>

  def testSb(addr: Long, src: Long): Unit = {
    val requiredData = src & ((1L << byteLen) - 1)
    var data: Long = 0L
    for (_ <- 1 to XLEN / byteLen) data = (data << byteLen) | requiredData

    val idx = addr & 7
    val strb = idx match {
      case 0 => (1 << 0)
      case 1 => (1 << 1)
      case 2 => (1 << 2)
      case 3 => (1 << 3)
      case 4 => (1 << 4)
      case 5 => (1 << 5)
      case 6 => (1 << 6)
      case 7 => (1 << 7)
      case _: Long => 0
    }

    it should s"sb 0x${addr.toHexString} 0x${src.toHexString} is 0x${data.toHexString}, ${strb.toBinaryString}" in {
      simulate(new PreLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(SB)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
        dut.io.strb.expect(s"h${strb.toHexString}".U)
      }
    }
  }

  def testSh(addr: Long, src: Long): Unit = {
    val requiredData = src & ((1L << halfLen) - 1)
    var data: Long = 0L
    for (_ <- 1 to XLEN / halfLen) data = (data << halfLen) | requiredData

    val idx = (addr & 6) >> 1
    val strb = idx match {
      case 0 => (3 << 0)
      case 1 => (3 << 2)
      case 2 => (3 << 4)
      case 3 => (3 << 6)
      case _: Long => 0
    }

    it should s"sh 0x${addr.toHexString} 0x${src.toHexString} is 0x${data.toHexString}, ${strb.toBinaryString}" in {
      simulate(new PreLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(SH)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
        dut.io.strb.expect(s"h${strb.toHexString}".U)
      }
    }
  }

  def testSw(addr: Long, src: Long): Unit = {
    val requiredData = src & ((1L << wordLen) - 1)
    var data: Long = 0L
    for (_ <- 1 to XLEN / wordLen) data = (data << wordLen) | requiredData

    val idx = (addr & 4) >> 2
    val strb = idx match {
      case 0 => (15 << 0)
      case 1 => (15 << 4)
      case _: Long => 0
    }

    it should s"sw 0x${addr.toHexString} 0x${src.toHexString} is 0x${data.toHexString}, ${strb.toBinaryString}" in {
      simulate(new PreLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(SW)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
        dut.io.strb.expect(s"h${strb.toHexString}".U)
      }
    }
  }

  def testSd(addr: Long, src: Long): Unit = {
    val data = src
    val strb = 255

    it should s"sd 0x${addr.toHexString} 0x${src.toHexString} is 0x${data.toHexString}, ${strb.toBinaryString}" in {
      simulate(new PreLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(SD)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
        dut.io.strb.expect(s"h${strb.toHexString}".U)
      }
    }
  }
}

class PreLsuTest extends AnyFlatSpec with PreLsuBehavior {
  behavior of "PreLsu"

  val rand = new Random
  val testNum = 10

  val addr = for (_ <- 0 until testNum) yield rand.nextLong()
  val src = for (_ <- 0 until testNum) yield rand.nextLong()

  for (i <- 0 until testNum) {
    testSb(addr(i), src(i))
    testSh(addr(i), src(i))
    testSw(addr(i), src(i))
    testSd(addr(i), src(i))
  }
}
