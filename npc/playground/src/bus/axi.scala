package bus

import chisel3._
import core.CoreConfig._

object BrustType extends ChiselEnum {
  val FIXED, INCR, WRAP = Value
}

object AccessType extends ChiselEnum {
  val NORMAL, EXCLUSIVE = Value
}

class Axi4ReadAddrBundle extends Bundle {
  val id        = UInt(1.W)
  val addr      = UInt(XLEN.W)
  val len       = UInt(8.W)
  val size      = UInt(3.W)
  val burst     = UInt(2.W)
  val lock      = UInt(1.W)
  val cache     = UInt(4.W)
  val prot      = UInt(3.W)
  val qos       = UInt(4.W)
  val region    = UInt(4.W)
}

object Axi4ReadAddrBundle {
  def apply(addr: UInt, len: UInt = 0.U, size: UInt): Axi4ReadAddrBundle = {
    val aw = Wire(new Axi4ReadAddrBundle)

    import BrustType._
    import AccessType._
    aw.id     := 0.U
    aw.addr   := addr
    aw.len    := len
    aw.size   := size
    aw.burst  := INCR
    aw.lock   := NORMAL
    aw.cache  := 0.U
    aw.prot   := 0.U
    aw.qos    := 0.U
    aw.region := 0.U

    aw
  }
}
