package bus

import chisel3._
import core.CoreConfig._
import chisel3.util.Decoupled

object BrustType extends ChiselEnum {
  val FIXED, INCR, WRAP = Value
}

object AccessType extends ChiselEnum {
  val NORMAL, EXCLUSIVE = Value
}

object ResponseType extends ChiselEnum {
  val OKAY, EXOKAY, SLVERR, DECERR = Value
}

class Axi4WriteAddrBundle extends Bundle {
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

object Axi4WriteAddrBundle {
  def apply(addr: UInt, len: UInt = 0.U, size: UInt): Axi4WriteAddrBundle = {
    val aw = Wire(new Axi4WriteAddrBundle)

    import BrustType._
    import AccessType._
    aw.id     := 0.U
    aw.addr   := addr
    aw.len    := len
    aw.size   := size
    aw.burst  := INCR.asUInt
    aw.lock   := NORMAL.asUInt
    aw.cache  := 0.U
    aw.prot   := 0.U
    aw.qos    := 0.U
    aw.region := 0.U

    aw
  }
}

class Axi4WriteDataBundle extends Bundle {
  val id      = UInt(1.W)
  val data    = UInt(XLEN.W)
  val strb    = UInt((XLEN / 8).W)
  val last    = Bool()
}

object Axi4WriteDataBundle {
  def apply(data: UInt, strb: UInt, last: Bool = false.B): Axi4WriteDataBundle = {
    val w = Wire(new Axi4WriteDataBundle)

    w.id    := 0.U
    w.data  := data
    w.strb  := strb
    w.last  := last

    w
  }
}

class Axi4WriteRespBundle extends Bundle {
  val id      = UInt(1.W)
  val resp    = UInt(2.W)
}

object Axi4WriteRespBundle {
  def apply(): Axi4WriteRespBundle = {
    val b = Wire(new Axi4WriteRespBundle)

    import ResponseType._
    b.id    := 0.U
    b.resp  := OKAY.asUInt

    b
  }
}

class Axi4ReadAddrBundle extends Bundle {
  val id      = UInt(1.W)
  val addr    = UInt(XLEN.W)
  val len     = UInt(8.W)
  val size    = UInt(3.W)
  val burst   = UInt(2.W)
  val lock    = UInt(1.W)
  val cache   = UInt(4.W)
  val prot    = UInt(3.W)
  val qos     = UInt(4.W)
  val region  = UInt(4.W)
}

object Axi4ReadAddrBundle {
  def apply(addr: UInt, len: UInt = 0.U, size: UInt): Axi4ReadAddrBundle = {
    val ar = Wire(new Axi4ReadAddrBundle)

    import BrustType._
    import AccessType._
    ar.id     := 0.U
    ar.addr   := addr
    ar.len    := len
    ar.size   := size
    ar.burst  := INCR.asUInt
    ar.lock   := NORMAL.asUInt
    ar.cache  := 0.U
    ar.prot   := 0.U
    ar.qos    := 0.U
    ar.region := 0.U

    ar
  }
}

class Axi4ReadDataBundle extends Bundle {
  val id      = UInt(1.W)
  val data    = UInt(XLEN.W)
  val resp    = UInt(2.W)
  val last    = Bool()
}

object Axi4ReadDataBundle {
  import ResponseType._
  def apply(data: UInt, resp: UInt = OKAY.asUInt, last: Bool = false.B): Axi4ReadDataBundle = {
    val r = Wire(new Axi4ReadDataBundle)

    r.id    := 0.U
    r.data  := data
    r.resp  := resp
    r.last  := last

    r
  }
}

class Axi4Bundle extends Bundle {
  val aw = Decoupled(new Axi4WriteAddrBundle)
  val w  = Decoupled(new Axi4WriteDataBundle)
  val b  = Flipped(Decoupled(new Axi4WriteRespBundle))
  val ar = Decoupled(new Axi4ReadAddrBundle)
  val r  = Flipped(Decoupled(new Axi4ReadDataBundle))
}
