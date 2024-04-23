package memory

import chisel3._
import chisel3.util.Decoupled
import bus.Axi4WriteAddrBundle
import bus.Axi4WriteDataBundle
import bus.Axi4WriteRespBundle
import chisel3.util.switch
import chisel3.util.is

object UartState extends ChiselEnum {
  val sIdle, sWrite, sFin = Value
}

class Uart extends Module {
  val io = IO(new Bundle {
    val axi = new Bundle {
      val aw = Flipped(Decoupled(new Axi4WriteAddrBundle))
      val w  = Flipped(Decoupled(new Axi4WriteDataBundle))
      val b  = Decoupled(new Axi4WriteRespBundle)
    }
  })

  import UartState._
  val stateReg = RegInit(sIdle)
  val nextState = WireDefault(sIdle)
  stateReg := nextState

  io.axi.aw.ready   := stateReg === sIdle
  io.axi.w.ready    := stateReg === sWrite
  io.axi.b.valid    := stateReg === sFin
  io.axi.b.bits     := Axi4WriteRespBundle()

  when (io.axi.w.fire) {
    printf(cf"${io.axi.w.bits.data(7, 0)}%c")
  }

  switch (stateReg) {
    is (sIdle) {
      nextState := Mux(io.axi.aw.fire, sWrite, sIdle)
    }

    is (sWrite) {
      nextState := Mux(io.axi.w.fire, sFin, sWrite)
    }

    is (sFin) {
      nextState := Mux(io.axi.b.fire, sIdle, sFin)
    }
  }
}
