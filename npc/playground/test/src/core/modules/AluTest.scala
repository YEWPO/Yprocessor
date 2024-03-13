package core.modules

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random
import AluOp._

trait AluBehavior {
  this: AnyFlatSpec =>

  def testAdd(src1: Long, src2: Long): Unit = {
    val res = src1 + src2

    it should s"$src1 + $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(ADD)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSll(src1: Long, src2: Long): Unit = {
    val shamt = src2 & 63
    val res = src1 << shamt

    it should s"$src1 << $shamt = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SLL)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSlt(src1: Long, src2: Long): Unit = {
    val res: Long = if (src1 < src2) 1 else 0

    it should s"$src1 < $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SLT)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSltu(src1: Long, src2: Long): Unit = {
    val res: Long = if ((src1 < src2) ^ (src1 < 0) ^ (src2 < 0)) 1 else 0

    it should s"$src1 <u $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SLTU)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testXor(src1: Long, src2: Long): Unit = {
    val res = src1 ^ src2

    it should s"$src1 ^ $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(XOR)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSrl(src1: Long, src2: Long): Unit = {
    val shamt = src2 & 63
    val res = src1 >>> shamt

    it should s"$src1 >>> $shamt = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SRL)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testOr(src1: Long, src2: Long): Unit = {
    val res = src1 | src2

    it should s"$src1 | $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(OR)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testAnd(src1: Long, src2: Long): Unit = {
    val res = src1 & src2

    it should s"$src1 & $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(AND)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSub(src1: Long, src2: Long): Unit = {
    val res = src1 - src2

    it should s"$src1 - $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SUB)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSra(src1: Long, src2: Long): Unit = {
    val shamt = src2 & 63
    val res = src1 >> shamt

    it should s"$src1 >> $shamt = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SRA)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testAddw(src1: Long, src2: Long): Unit = {
    val res = ((src1 & ((1L << 32) - 1))  +  (src2 & ((1L << 32) - 1))) << 32 >> 32

    it should s"$src1 +w $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(ADDW)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSubw(src1: Long, src2: Long): Unit = {
    val res = ((src1 & ((1L << 32) - 1))  -  (src2 & ((1L << 32) - 1))) << 32 >> 32

    it should s"$src1 -w $src2 = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SUBW)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSllw(src1: Long, src2: Long): Unit = {
    val shamt =  src2 & 31
    val res = ((src1 & ((1L << 32) - 1))  <<  shamt) << 32 >> 32

    it should s"$src1 <<w $shamt = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SLLW)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSrlw(src1: Long, src2: Long): Unit = {
    val shamt =  src2 & 31
    val res = ((src1 & ((1L << 32) - 1))  >>>  shamt) << 32 >> 32

    it should s"$src1 >>>w $shamt = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SRLW)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }

  def testSraw(src1: Long, src2: Long): Unit = {
    val shamt =  src2 & 31
    val res = (src1 & (1L << 32) - 1) << 32 >> 32  >>  shamt

    it should s"$src1 >>w $shamt = $res" in {
      simulate(new Alu) { dut =>
        dut.io.src1.poke(s"h${src1.toHexString}".U)
        dut.io.src2.poke(s"h${src2.toHexString}".U)
        dut.io.aluOp.poke(SRAW)
        dut.clock.step()
        dut.io.res.expect(s"h${res.toHexString}".U)
      }
    }
  }
}

class AluTest extends AnyFlatSpec with AluBehavior {
  behavior of "Alu"

  val rand = new Random

  val testNum = 15

  val src1 = for (_ <- 0 until testNum) yield rand.nextLong()
  val src2 = for (_ <- 0 until testNum) yield rand.nextLong()

  for (i <- 0 until testNum) {
    testAdd(src1(i), src2(i))
    testSll(src1(i), src2(i))
    testSlt(src1(i), src2(i))
    testSltu(src1(i), src2(i))
    testXor(src1(i), src2(i))
    testSrl(src1(i), src2(i))
    testOr(src1(i), src2(i))
    testAnd(src1(i), src2(i))

    testSub(src1(i), src2(i))
    testSra(src1(i), src2(i))

    testAddw(src1(i), src2(i))
    testSubw(src1(i), src2(i))
    testSllw(src1(i), src2(i))
    testSrlw(src1(i), src2(i))
    testSraw(src1(i), src2(i))
  }
}
