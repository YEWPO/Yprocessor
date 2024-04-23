package memory

import chisel3._
import bus.Axi4Bundle

class Peri extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(new Axi4Bundle)
  })

  val xbar = Module(new Xbar)
  
  val uart = Module(new Uart)
  val sram = Module(new Sram)

  xbar.io.axi <> io.axi

  uart.io.axi.aw <> xbar.io.uart.aw
  uart.io.axi.w  <> xbar.io.uart.w
  uart.io.axi.b  <> xbar.io.uart.b

  sram.axi <> xbar.io.sram
}
