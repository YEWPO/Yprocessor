import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class TopTest extends AnyFreeSpec with Matchers {
  "Top should calculate proper xor value" in {
    simulate(new Top) { dut =>
      dut.io.a.poke(true.B)
      dut.io.b.poke(false.B)
      dut.clock.step()
      dut.io.c.expect(true.B)
    }
  }
}
