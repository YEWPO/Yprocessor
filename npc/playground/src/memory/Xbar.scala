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
import bus.Axi4ReadDataBundle
import bus.Axi4ReadAddrBundle

object XbarReadState extends ChiselEnum {
  val rIdle, rTimer, rSram = Value
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

    val timer = new Bundle {
      val ar = Decoupled(new Axi4ReadAddrBundle)
      val r  = Flipped(Decoupled(new Axi4ReadDataBundle))
    }

    val sram = new Axi4Bundle
  })

  io.sram.ar      <> io.axi.ar
  io.sram.r       <> io.axi.r

  import XbarReadState._
  import XbarWriteState._

  val readStateReg     = RegInit(rIdle)
  val readNextState    = WireDefault(rIdle)
  readStateReg := readNextState

  val writeStateReg    = RegInit(wIdle)
  val writeNextState   = WireDefault(wIdle)
  writeStateReg := writeNextState

  val timerRead   = io.axi.ar.valid && (io.axi.ar.bits.addr === TIMER_ADDR.U)
  val sramRead    = io.axi.ar.valid && (io.axi.ar.bits.addr >= MEM_ADDR_BASE.U) && (io.axi.ar.bits.addr < MEM_ADDR_MAX.U)

  io.timer.ar.valid := Mux(readStateReg === rTimer, io.axi.ar.valid, false.B)
  io.timer.ar.bits  := io.axi.ar.bits
  io.timer.r.ready  := Mux(readStateReg === rTimer, io.axi.r.ready, false.B)

  io.sram.ar.valid := Mux(readStateReg === rSram, io.axi.ar.valid, false.B)
  io.sram.ar.bits  := io.axi.ar.bits
  io.sram.r.ready  := Mux(readStateReg === rSram, io.axi.r.ready, false.B)

  io.axi.ar.ready := MuxCase(false.B, Seq(
    (readStateReg === rTimer) -> io.timer.ar.ready,
    (readStateReg === rSram)  -> io.sram.ar.ready
  ))
  io.axi.r.valid := MuxCase(false.B, Seq(
    (readStateReg === rTimer) -> io.timer.r.valid,
    (readStateReg === rSram)  -> io.sram.r.valid
  ))
  io.axi.r.bits  := Mux(readStateReg === rTimer, io.timer.r.bits, io.sram.r.bits)

  val uartWrite   = io.axi.aw.valid && (io.axi.aw.bits.addr === UART_ADDR.U)
  val sramWrite   = io.axi.aw.valid && (io.axi.aw.bits.addr >= MEM_ADDR_BASE.U) && (io.axi.aw.bits.addr < MEM_ADDR_MAX.U)

  io.uart.aw.valid := Mux(writeStateReg === wUart, io.axi.aw.valid, false.B)
  io.uart.aw.bits  := io.axi.aw.bits
  io.uart.w.valid  := Mux(writeStateReg === wUart, io.axi.w.valid, false.B)
  io.uart.w.bits   := io.axi.w.bits
  io.uart.b.ready  := Mux(writeStateReg === wUart, io.axi.b.ready, false.B)

  io.sram.aw.valid := Mux(writeStateReg === wSram, io.axi.aw.valid, false.B)
  io.sram.aw.bits  := io.axi.aw.bits
  io.sram.w.valid  := Mux(writeStateReg === wSram, io.axi.w.valid, false.B)
  io.sram.w.bits   := io.axi.w.bits
  io.sram.b.ready  := Mux(writeStateReg === wSram, io.axi.b.ready, false.B)

  io.axi.aw.ready := MuxCase(false.B, Seq(
    (writeStateReg === wUart) -> io.uart.aw.ready,
    (writeStateReg === wSram) -> io.sram.aw.ready
  ))
  io.axi.w.ready  := MuxCase(false.B, Seq(
    (writeStateReg === wUart) -> io.uart.w.ready,
    (writeStateReg === wSram) -> io.sram.w.ready
  ))
  io.axi.b.valid  := MuxCase(false.B, Seq(
    (writeStateReg === wUart) -> io.uart.b.valid,
    (writeStateReg === wSram) -> io.sram.b.valid
  ))
  io.axi.b.bits   := Mux(writeStateReg === wUart, io.uart.b.bits, io.sram.b.bits)

  switch (readStateReg) {
    is (rIdle) {
      readNextState := MuxCase(rIdle, Seq(
        timerRead -> rTimer,
        sramRead -> rSram
      ))
    }

    is (rTimer) {
      readNextState := MuxCase(rTimer, Seq(
        (io.timer.r.fire && io.timer.r.bits.last && timerRead) -> rTimer,
        (io.timer.r.fire && io.timer.r.bits.last && sramRead) -> rSram,
        io.timer.r.fire -> rIdle
      ))
    }

    is (rSram) {
      readNextState := MuxCase(rSram, Seq(
        (io.sram.r.fire && io.sram.r.bits.last && timerRead) -> rTimer,
        (io.sram.r.fire && io.sram.r.bits.last && sramRead) -> rSram,
        (io.sram.r.fire && io.sram.r.bits.last) -> rIdle
      ))
    }
  }

  switch (writeStateReg) {
    is (wIdle) {
      writeNextState := MuxCase(wIdle, Seq(
        uartWrite -> wUart,
        sramWrite -> wSram
      ))
    }

    is (wUart) {
      writeNextState := MuxCase(wUart, Seq(
        (io.uart.b.fire && uartWrite) -> wUart,
        (io.uart.b.fire && sramWrite) -> wSram,
        io.uart.b.fire -> wIdle
      ))
    }

    is (wSram) {
      writeNextState := MuxCase(wSram, Seq(
        (io.sram.b.fire && uartWrite) -> wUart,
        (io.sram.b.fire && sramWrite) -> wSram,
        io.sram.b.fire -> wIdle
      ))
    }
  }
}
