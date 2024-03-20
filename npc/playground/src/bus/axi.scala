package bus

import chisel3._
import core.CoreConfig._

class AxiReadAddrBundle extends Bundle {
  val id        = UInt(1.W)
  val addr      = UInt(XLEN.W)
  val len       = UInt(8.W)
  val size      = UInt(3.W)
  val burst     = UInt(2.W)
  val lock      = UInt(2.W)
  val cache     = UInt(4.W)
  val prot      = UInt(3.W)
  val qos       = UInt(4.W)
  val region    = UInt(4.W)
}
