package memory

import chisel3._
import bus.Axi4Bundle
import chisel3.util.Decoupled
import bus.Axi4ReadAddrBundle
import bus.Axi4ReadDataBundle
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.Counter
import chisel3.util.HasBlackBoxPath
import chisel3.util.Cat

object SramReadState extends ChiselEnum {
  val rWait, rRead = Value
}

class SramReadBlackBox extends BlackBox with HasBlackBoxPath {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val en  = Input(Bool())
    val addr = Input(UInt(32.W))
    val data = Output(UInt(32.W))
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

  val (readCount, readDone) = Counter(r.fire, 2)

  val sramRead = Module(new SramReadBlackBox)
  sramRead.io.clk := clock
  sramRead.io.en  := stateReg === rRead
  sramRead.io.addr := arReg.addr + Cat(readCount, 0.U(3.W))

  ar.ready      := nextState === rWait

  r.valid       := stateReg === rRead
  r.bits        := Axi4ReadDataBundle(
    data = sramRead.io.data,
    last = readCount === 1.U
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

class Sram extends Module {
  val axi = new Axi4Bundle

  val read = Module(new SramRead)

  read.ar     <> axi.ar
  read.r      <> axi.r
}
