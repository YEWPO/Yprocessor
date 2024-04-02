package core.modules

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random
import core.CoreConfig._

case class GprInputs(
  rdWbu:      Int,
  dataWbu:    Long,
  rdLsu:      Int,
  dataLsu:    Long,

  rs1:        Int,
  rs2:        Int
)

trait GprBehavior {
  this: AnyFlatSpec =>

  def testGpr(idx: Int, input: GprInputs): Unit = {
    val src1 = if (input.rdLsu == input.rs1) input.dataLsu else if (input.rdWbu == input.rs1) input.dataWbu else 0
    val src2 = if (input.rdLsu == input.rs2) input.dataLsu else if (input.rdWbu == input.rs2) input.dataWbu else 0

    it should s"test $idx: x${input.rs1} is 0x${src1.toHexString}, x${input.rs2} is 0x${src2.toHexString}" in {
      simulate(new Gpr) { dut =>
        dut.io.rdWbu.poke(s"h${input.rdWbu.toHexString}".U)
        dut.io.rdLsu.poke(s"h${input.rdLsu.toHexString}".U)
        dut.io.dataWbu.poke(s"h${input.dataWbu.toHexString}".U)
        dut.io.dataLsu.poke(s"h${input.dataLsu.toHexString}".U)
        dut.io.rsIn.valid.poke(true.B)
        dut.io.rsIn.bits.rs1.poke(s"h${input.rs1.toHexString}".U)
        dut.io.rsIn.bits.rs2.poke(s"h${input.rs2.toHexString}".U)
        dut.clock.step()
        dut.io.src1.expect(s"h${src1.toHexString}".U)
        dut.io.src2.expect(s"h${src2.toHexString}".U)
      }
    }
  }
}

class GprTest extends AnyFlatSpec with GprBehavior {
  behavior of "Gpr"

  val rand = new Random
  val testNum = 50

  val rdWbu     = for (_ <- 0 to testNum) yield rand.nextInt(GPR_NUM)
  val rdLsu     = for (_ <- 0 to testNum) yield rand.nextInt(GPR_NUM)
  val dataWbu   = for (_ <- 0 to testNum) yield rand.nextLong()
  val dataLsu   = for (_ <- 0 to testNum) yield rand.nextLong()
  val rs1       = for (_ <- 0 to testNum) yield rand.nextInt(GPR_NUM)
  val rs2       = for (_ <- 0 to testNum) yield rand.nextInt(GPR_NUM)

  for (i <- 0 to testNum) testGpr(i, GprInputs(rdWbu(i), dataWbu(i), rdLsu(i), dataLsu(i), rs1(i), rs2(i)))
}
