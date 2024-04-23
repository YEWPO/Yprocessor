package memory

import chisel3._
import bus.Axi4ReadAddrBundle
import chisel3.util.Decoupled
import bus.Axi4ReadDataBundle
import chisel3.util.switch
import chisel3.util.is
import core.CoreConfig._

object TimerState extends ChiselEnum {
  val sIdle, sRead = Value
}

class Timer extends Module {
  val axi = IO(new Bundle {
    val ar = Flipped(Decoupled(new Axi4ReadAddrBundle))
    val r  = Decoupled(new Axi4ReadDataBundle)
  })

  import TimerState._
  val stateReg    = RegInit(sIdle)
  val nextState   = WireDefault(sIdle)
  stateReg        := nextState

  val timer       = RegNext(0.U(XLEN.W))
  timer           := timer + 1.U

  axi.ar.ready    := stateReg === sIdle
  axi.r.valid     := stateReg === sRead
  axi.r.bits      := Axi4ReadDataBundle(
    data = timer,
    last = true.B
  )

  switch (stateReg) {
    is (sIdle) {
      nextState := Mux(axi.ar.fire, sRead, sIdle)
    }

    is (sRead) {
      nextState := Mux(axi.r.fire, sIdle, sRead)
    }
  }
}
