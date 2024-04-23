package memory

import chisel3._
import bus.Axi4Bundle

class Peri extends Module {
  val axi   = IO(Flipped(new Axi4Bundle))

  val xbar  = Module(new Xbar)
  
  val uart  = Module(new Uart)
  val sram  = Module(new Sram)
  val timer = Module(new Timer)

  xbar.io.axi <> axi

  uart.axi.aw <> xbar.io.uart.aw
  uart.axi.w  <> xbar.io.uart.w
  uart.axi.b  <> xbar.io.uart.b

  timer.axi.ar <> xbar.io.timer.ar
  timer.axi.r  <> xbar.io.timer.r

  sram.axi <> xbar.io.sram
}
