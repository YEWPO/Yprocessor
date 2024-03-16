package core.modules

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

trait PcGenBehavior {
  this: AnyFlatSpec =>

  def testPcGen(pc: Long, instLen: Int, control: Boolean, dnpc: Long): Unit = {
    val snpc = if (instLen == 3) pc + 4 else pc + 2
    val npc = if (control) dnpc else snpc

    it should s"pc 0x${pc.toHexString} $instLen, snpc = 0x${snpc.toHexString} npc = 0x${dnpc.toHexString}" in {
      simulate(new PcGen) { dut =>
        dut.io.pc.poke(s"h${pc.toHexString}".U)
        dut.io.dnpc.poke(s"h${dnpc.toHexString}".U)
        dut.io.instLen.poke(s"h${instLen.toHexString}".U)
        dut.io.control.poke(control.B)
        dut.clock.step()
        dut.io.snpc.expect(s"h${snpc.toHexString}".U)
        dut.io.npc.expect(s"h${npc.toHexString}".U)
      }
    }
  }
}

class PcGenTest extends AnyFlatSpec with PcGenBehavior {
  behavior of "PcGen"

  val rand = new Random
  val testNum = 10

  val pc = for (_ <- 0 until testNum) yield rand.nextLong()
  val dnpc = for (_ <- 0 until testNum) yield rand.nextLong()
  val instLen = for (_ <- 0 until testNum) yield rand.nextInt(4)
  val control = for (_ <- 0 until testNum) yield rand.nextBoolean()

  for (i <- 0 until testNum) {
    testPcGen(pc(i), instLen(i), control(i), dnpc(i))
  }
}
