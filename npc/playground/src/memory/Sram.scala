package memory

import chisel3._
import bus.Axi4Bundle
import chisel3.util.Decoupled
import bus.Axi4ReadAddrBundle
import bus.Axi4ReadDataBundle
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.HasBlackBoxPath
import chisel3.util.Cat
import bus.Axi4WriteAddrBundle
import bus.Axi4WriteDataBundle
import bus.Axi4WriteRespBundle
import core.CoreConfig._

object SramReadState extends ChiselEnum {
  val rWait, rRead = Value
}

class SramReadBlackBox extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val en  = Input(Bool())
    val addr = Input(UInt(XLEN.W))
    val data = Output(UInt(XLEN.W))
  })

  addPath("src/memory/SramRead.v")
}

class SramRead extends Module {
  val ar = Flipped(Decoupled(new Axi4ReadAddrBundle))
  val r  = Decoupled(new Axi4ReadDataBundle)

  import SramReadState._
  val stateReg      = RegInit(rWait)
  val nextState     = Wire(SramReadState())

  val arReg         = Reg(new Axi4ReadAddrBundle)
  arReg := Mux(ar.fire, ar.bits, arReg)

  val readDone      = r.fire && r.bits.last
  val readCnt       = RegInit(0.U(8.W))
  readCnt := Mux(nextState === rWait, 0.U, Mux(r.fire, readCnt + 1.U, readCnt))

  val sramRead = Module(new SramReadBlackBox)
  sramRead.io.clk := clock
  sramRead.io.en  := stateReg === rRead
  sramRead.io.addr := arReg.addr + Cat(readCnt, 0.U(3.W))

  ar.ready      := nextState === rWait

  r.valid       := stateReg === rRead
  r.bits        := Axi4ReadDataBundle(
    data = sramRead.io.data,
    last = readCnt === 1.U
  )

  nextState := rWait
  switch (stateReg) {
    is (rWait) {
      nextState := Mux(ar.fire, rRead, rWait)
    }

    is (rRead) {
      nextState := Mux(readDone, rWait, rRead)
    }
  }
}

object SramWriteState extends ChiselEnum {
  val wWait, wWrite, wResp = Value
}

class SramWriteBlackBox extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val clk     = Input(Clock())
    val en      = Input(Bool())
    val addr    = Input(UInt(XLEN.W))
    val data    = Input(UInt(XLEN.W))
    val strb    = Input(UInt(8.W))
  })

  addPath("src/memory/SramWrite.v")
}

class SramWrite extends Module {
  val aw = Flipped(Decoupled(new Axi4WriteAddrBundle))
  val w  = Flipped(Decoupled(new Axi4WriteDataBundle))
  val b  = Decoupled(new Axi4WriteRespBundle)

  import SramWriteState._
  val stateReg      = RegInit(wWait)
  val nextState     = Wire(SramWriteState())
  stateReg := nextState

  val awReg         = Reg(new Axi4WriteAddrBundle)
  awReg := Mux(aw.fire, aw.bits, awReg)

  val writeDone     = w.fire && w.bits.last
  val writeCnt      = RegInit(0.U(8.W))
  writeCnt := Mux(nextState === wWait, 0.U, Mux(w.fire, writeCnt + 1.U, writeCnt))

  aw.ready         := nextState === wWait
  w.ready          := stateReg === wWrite
  b.valid          := stateReg === wResp
  b.bits           := Axi4WriteRespBundle()

  val sramWrite = Module(new SramWriteBlackBox)
  sramWrite.io.clk    := clock
  sramWrite.io.en     := stateReg === wWrite
  sramWrite.io.addr   := awReg.addr + Cat(writeCnt, 0.U(3.W))
  sramWrite.io.data   := w.bits.data
  sramWrite.io.strb   := w.bits.strb

  nextState := wWait
  switch (stateReg) {
    is (wWait) {
      nextState := Mux(aw.fire, wWrite, wWait)
    }

    is (wWrite) {
      nextState := Mux(writeDone, wResp, wWrite)
    }

    is (wResp) {
      nextState := Mux(b.fire, wWait, wResp)
    }
  }
}

class Sram extends Module {
  val axi = new Axi4Bundle

  val read  = Module(new SramRead)
  val write = Module(new SramWrite)

  read.ar     <> axi.ar
  read.r      <> axi.r
  write.aw    <> axi.aw
  write.w     <> axi.w
  write.b     <> axi.b
}
