package memory

import chisel3._
import bus.Axi4Bundle
import chisel3.util.Decoupled
import chisel3.util.switch
import chisel3.util.is
import MemoryConfig._
import chisel3.util.MuxCase

object XbarReadState extends ChiselEnum {
}

object XbarWriteState extends ChiselEnum {
  val wIdle, wUart, wSram = Value
}

class Xbar extends Module {
  val io = IO(new Bundle {
    val axi = Flipped(new Axi4Bundle)

    val uart = new Bundle {
      val aw = Decoupled(new Axi4Bundle)
      val w  = Decoupled(new Axi4Bundle)
      val b  = Flipped(Decoupled(new Axi4Bundle))
    }

    val sram = new Axi4Bundle
  })

  io.sram.ar      <> io.axi.ar
  io.sram.r       <> io.axi.r

  import XbarWriteState._
  val stateReg    = RegInit(wIdle)
  val nextState   = WireDefault(wIdle)
  stateReg        := nextState

  val uartWrite   = io.axi.aw.bits.addr === 0xa00003f8.U
  val sramWrite   = (io.axi.aw.bits.addr >= MEM_ADDR_BASE.U) && (io.axi.aw.bits.addr < MEM_ADDR_MAX.U)

  io.axi.aw.ready := stateReg === wIdle
  io.axi.w.ready  := MuxCase(false.B, Seq(
    (stateReg === wUart) -> io.uart.w.ready,
    (stateReg === wSram) -> io.sram.w.ready
  ))
  io.axi.b.valid  := MuxCase(false.B, Seq(
    (stateReg === wUart) -> io.uart.b.valid,
    (stateReg === wSram) -> io.sram.b.valid
  ))
  io.axi.b.bits   := MuxCase(io.uart.b.bits, Seq(
    (stateReg === wUart) -> io.uart.b.bits,
    (stateReg === wSram) -> io.sram.b.bits
  ))

  switch (stateReg) {
    is (wIdle) {
      nextState := MuxCase(wIdle, Seq(
        (io.axi.aw.fire && uartWrite) -> wUart,
        (io.axi.aw.fire && sramWrite) -> wSram
      ))
    }

    is (wUart) {
      nextState := MuxCase(wUart, Seq(
        (io.uart.b.fire && io.axi.aw.fire && uartWrite) -> wUart,
        (io.uart.b.fire && io.axi.aw.fire && sramWrite) -> wSram,
        io.uart.b.fire -> wIdle
      ))
    }

    is (wSram) {
      nextState := MuxCase(wSram, Seq(
        (io.sram.b.fire && io.axi.aw.fire && uartWrite) -> wUart,
        (io.sram.b.fire && io.axi.aw.fire && sramWrite) -> wSram,
        io.sram.b.fire -> wIdle
      ))
    }
  }
}
