package core.modules

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import core.stages.LsuOp._
import scala.util.Random

trait SufLsuBehavior {
  this: AnyFlatSpec =>

  def testLb(addr: Long, src: Long): Unit = {
    val idx = addr & 7
    val data = idx match {
      case 0 => src >> 0  << 56 >> 56
      case 1 => src >> 8  << 56 >> 56
      case 2 => src >> 16 << 56 >> 56
      case 3 => src >> 24 << 56 >> 56
      case 4 => src >> 32 << 56 >> 56
      case 5 => src >> 40 << 56 >> 56
      case 6 => src >> 48 << 56 >> 56
      case 7 => src >> 56 << 56 >> 56
    }

    it should s"lb 0x${addr.toHexString} 0x${src.toHexString}, 0x${data.toHexString}" in {
      simulate(new SufLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(LB)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
      }
    }
  }

  def testLh(addr: Long, src: Long): Unit = {
    val idx = (addr & 6) >> 1
    val data = idx match {
      case 0 => src >> 0  << 48 >> 48
      case 1 => src >> 16 << 48 >> 48
      case 2 => src >> 32 << 48 >> 48
      case 3 => src >> 48 << 48 >> 48
    }

    it should s"lh 0x${addr.toHexString} 0x${src.toHexString}, 0x${data.toHexString}" in {
      simulate(new SufLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(LH)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
      }
    }
  }

  def testLw(addr: Long, src: Long): Unit = {
    val idx = (addr & 4) >> 2
    val data = idx match {
      case 0 => src >> 0  << 32 >> 32
      case 1 => src >> 32 << 32 >> 32
    }

    it should s"lw 0x${addr.toHexString} 0x${src.toHexString}, 0x${data.toHexString}" in {
      simulate(new SufLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(LW)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
      }
    }
  }

  def testLd(addr: Long, src: Long): Unit = {
    val data = src

    it should s"ld 0x${addr.toHexString} 0x${src.toHexString}, 0x${data.toHexString}" in {
      simulate(new SufLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(LD)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
      }
    }
  }

  def testLbu(addr: Long, src: Long): Unit = {
    val idx = addr & 7
    val data = idx match {
      case 0 => src >> 0  << 56 >>> 56
      case 1 => src >> 8  << 56 >>> 56
      case 2 => src >> 16 << 56 >>> 56
      case 3 => src >> 24 << 56 >>> 56
      case 4 => src >> 32 << 56 >>> 56
      case 5 => src >> 40 << 56 >>> 56
      case 6 => src >> 48 << 56 >>> 56
      case 7 => src >> 56 << 56 >>> 56
    }

    it should s"lbu 0x${addr.toHexString} 0x${src.toHexString}, 0x${data.toHexString}" in {
      simulate(new SufLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(LBU)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
      }
    }
  }

  def testLhu(addr: Long, src: Long): Unit = {
    val idx = (addr & 6) >> 1
    val data = idx match {
      case 0 => src >> 0  << 48 >>> 48
      case 1 => src >> 16 << 48 >>> 48
      case 2 => src >> 32 << 48 >>> 48
      case 3 => src >> 48 << 48 >>> 48
    }

    it should s"lhu 0x${addr.toHexString} 0x${src.toHexString}, 0x${data.toHexString}" in {
      simulate(new SufLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(LHU)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
      }
    }
  }

  def testLwu(addr: Long, src: Long): Unit = {
    val idx = (addr & 4) >> 2
    val data = idx match {
      case 0 => src >> 0  << 32 >>> 32
      case 1 => src >> 32 << 32 >>> 32
    }

    it should s"lwu 0x${addr.toHexString} 0x${src.toHexString}, 0x${data.toHexString}" in {
      simulate(new SufLsu) { dut =>
        dut.io.addr.poke(s"h${addr.toHexString}".U)
        dut.io.src.poke(s"h${src.toHexString}".U)
        dut.io.lsuOp.poke(LWU)
        dut.clock.step()
        dut.io.data.expect(s"h${data.toHexString}".U)
      }
    }
  }
}

class SufLsuTest extends AnyFlatSpec with SufLsuBehavior {
  behavior of "SufLsu"

  val rand = new Random
  val testNum = 20

  val addr = for (_ <- 0 until testNum) yield rand.nextLong()
  val src = for (_ <- 0 until testNum) yield rand.nextLong()

  for (i <- 0 until testNum) {
    testLb(addr(i), src(i))
    testLh(addr(i), src(i))
    testLw(addr(i), src(i))
    testLd(addr(i), src(i))
    testLbu(addr(i), src(i))
    testLhu(addr(i), src(i))
    testLwu(addr(i), src(i))
  }
}
