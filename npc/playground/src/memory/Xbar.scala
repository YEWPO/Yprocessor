package memory

import chisel3._
import bus.Axi4Bundle
import chisel3.util.Decoupled
import chisel3.util.switch
import chisel3.util.is
import MemoryConfig._
import chisel3.util.MuxCase
import bus.Axi4WriteAddrBundle
import bus.Axi4WriteDataBundle
import bus.Axi4WriteRespBundle

object XbarReadState extends ChiselEnum {
}

object XbarWriteState extends ChiselEnum {
  val wIdle, wUart, wSram = Value
}

class Xbar extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(new Axi4Bundle)

    val uart = new Bundle {
      val aw = Decoupled(new Axi4WriteAddrBundle)
      val w  = Decoupled(new Axi4WriteDataBundle)
      val b  = Flipped(Decoupled(new Axi4WriteRespBundle))
    }

    val sram = new Axi4Bundle
  })

  io.sram.ar      <> io.axi.ar
  io.sram.r       <> io.axi.r

  import XbarWriteState._
  val stateReg    = RegInit(wIdle)
  val nextState   = WireDefault(wIdle)
  stateReg        := nextState

  val uartWrite   = io.axi.aw.bits.addr === UART_ADDR.U
  val sramWrite   = (io.axi.aw.bits.addr >= MEM_ADDR_BASE.U) && (io.axi.aw.bits.addr < MEM_ADDR_MAX.U)

  io.uart.aw.valid := Mux(stateReg === wUart, io.axi.aw.valid, false.B)
  io.uart.aw.bits  := io.axi.aw.bits
  io.uart.w.valid  := Mux(stateReg === wUart, io.axi.w.valid, false.B)
  io.uart.w.bits   := io.axi.w.bits
  io.uart.b.ready  := Mux(stateReg === wUart, io.axi.b.ready, false.B)

  io.sram.aw.valid := Mux(stateReg === wSram, io.axi.aw.valid, false.B)
  io.sram.aw.bits  := io.axi.aw.bits
  io.sram.w.valid  := Mux(stateReg === wSram, io.axi.w.valid, false.B)
  io.sram.w.bits   := io.axi.w.bits
  io.sram.b.ready  := Mux(stateReg === wSram, io.axi.b.ready, false.B)

  io.axi.aw.ready := MuxCase(false.B, Seq(
    (stateReg === wUart) -> io.uart.aw.ready,
    (stateReg === wSram) -> io.sram.aw.ready
  ))
  io.axi.w.ready  := MuxCase(false.B, Seq(
    (stateReg === wUart) -> io.uart.w.ready,
    (stateReg === wSram) -> io.sram.w.ready
  ))
  io.axi.b.valid  := MuxCase(false.B, Seq(
    (stateReg === wUart) -> io.uart.b.valid,
    (stateReg === wSram) -> io.sram.b.valid
  ))
  io.axi.b.bits   := Mux(stateReg === wUart, io.uart.b.bits, io.sram.b.bits)

  switch (stateReg) {
    is (wIdle) {
      nextState := MuxCase(wIdle, Seq(
        uartWrite -> wUart,
        sramWrite -> wSram
      ))
    }

    is (wUart) {
      nextState := MuxCase(wUart, Seq(
        (io.uart.b.fire && uartWrite) -> wUart,
        (io.uart.b.fire && sramWrite) -> wSram,
        io.uart.b.fire -> wIdle
      ))
    }

    is (wSram) {
      nextState := MuxCase(wSram, Seq(
        (io.sram.b.fire && uartWrite) -> wUart,
        (io.sram.b.fire && sramWrite) -> wSram,
        io.sram.b.fire -> wIdle
      ))
    }
  }
}
