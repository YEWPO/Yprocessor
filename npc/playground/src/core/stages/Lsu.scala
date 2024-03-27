package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._
import LsuOp._
import chisel3.util.Valid
import bus.Axi4Bundle
import core.modules.Cache
import chisel3.util.switch
import chisel3.util.is
import memory.MemoryConfig._
import chisel3.util.Counter
import bus.Axi4ReadAddrBundle
import chisel3.util.log2Up
import bus.Axi4WriteDataBundle

object LsuOp {
  val lsuOpLen = 5

  val byteLen = 8
  val halfLen = 2 * byteLen
  val wordLen = 2 * halfLen

  val W_TAG     = 4
  val R_TAG     = 3

  val LB    = "b0_1_000".U
  val LH    = "b0_1_001".U
  val LW    = "b0_1_010".U
  val LD    = "b0_1_011".U
  val LBU   = "b0_1_100".U
  val LHU   = "b0_1_101".U
  val LWU   = "b0_1_110".U

  val SB    = "b1_0_000".U
  val SH    = "b1_0_001".U
  val SW    = "b1_0_010".U
  val SD    = "b1_0_011".U
}

object LsuArbiterState extends ChiselEnum {
  val sIdle, sReadCache, sReadInit, sReadData, sWriteInit, sWriteData, sWriteFin = Value
}

class LsArbiter extends Module {
  val io = IO(new Bundle {
    val lsInfo = Flipped(Valid(new Bundle {
      val addr         = Input(UInt(XLEN.W))
      val wdata        = Input(UInt(XLEN.W))
      val wstrb        = Input(UInt((XLEN / 8).W))
    }))

    val axi     = new Axi4Bundle

    val data    = Output(UInt(XLEN.W))
    val finish  = Output(Bool())
  })

  import LsuArbiterState._
  val stateReg      = RegInit(sIdle)
  val nextState     = Wire(LsuArbiterState())
  stateReg := nextState

  val dcache        = Module(new Cache)

  val isRead        = io.lsInfo.valid && !io.lsInfo.bits.wstrb.orR
  val isWrite       = io.lsInfo.valid && io.lsInfo.bits.wstrb.orR
  val memoryOp      = (io.lsInfo.bits.addr >= MEM_ADDR_BASE.U) && (io.lsInfo.bits.addr < MEM_ADDR_MAX.U)

  val isReadCache   = isRead && memoryOp
  val isReadDevice  = isRead && !memoryOp

  val (readCount, readDone)   = Counter(io.axi.r.fire, 1)
  val (writeCount, writeDone) = Counter(io.axi.w.fire, 1)

  dcache.io.request.valid       := nextState === sReadCache
  dcache.io.request.bits.addr   := io.lsInfo.bits.addr
  dcache.io.abort               := false.B

  io.axi.ar.valid               := RegNext(nextState === sReadInit)
  io.axi.ar.bits                := RegNext(Axi4ReadAddrBundle(
    io.lsInfo.bits.addr,
    0.U,
    log2Up(XLEN / 8).U
  ))

  val readData  = Reg(UInt(XLEN.W))
  val readFin   = RegNext(readDone)
  io.axi.r.ready                := RegNext(nextState === sReadData)
  when (io.axi.r.fire) {
    readData := io.axi.r.bits.data
  }

  io.axi.aw.valid               := RegNext(nextState === sWriteInit)
  io.axi.aw.bits                := RegNext(Axi4ReadAddrBundle(
    io.lsInfo.bits.addr,
    0.U,
    log2Up(XLEN / 8).U
  ))

  io.axi.w.valid                := RegNext(nextState === sWriteData)
  io.axi.w.bits                 := RegNext(Axi4WriteDataBundle(
    io.lsInfo.bits.wdata,
    io.lsInfo.bits.wstrb,
    true.B
  ))

  val writeFin = RegNext(io.axi.b.fire)
  io.axi.b.ready                := RegNext(nextState === sWriteFin)

  nextState := sIdle
  switch (stateReg) {
    is (sIdle) {
      when (isReadCache) {
        nextState := sReadCache
      } .elsewhen (isReadDevice) {
        nextState := sReadInit
      } .elsewhen (isWrite) {
        nextState := sWriteInit
      }
    }

    is (sReadCache) {
      nextState := Mux(dcache.io.response.valid, sIdle, sReadCache)
    }

    is (sReadInit) {
      nextState := Mux(io.axi.ar.fire, sReadData, sReadInit)
    }

    is(sReadData) {
      nextState := Mux(readDone, sIdle, sReadData)
    }

    is (sWriteInit) {
      nextState := Mux(io.axi.aw.fire, sWriteData, sWriteInit)
    }

    is (sWriteData) {
      nextState := Mux(writeDone, sWriteFin, sWriteData)
    }

    is (sWriteFin) {
      nextState := Mux(io.axi.b.fire, sIdle, sWriteFin)
    }
  }
}

class Lsu extends Module {
  val io = IO(new Bundle {
    val lsuIn = Flipped(Decoupled(new Bundle {
      val rd          = UInt(5.W)
      val exuRes      = UInt(XLEN.W)
      val lsuOp       = UInt(lsuOpLen.W)
      val kill        = Bool()
      val invalid     = Bool()
      val pc          = UInt(XLEN.W)
    }))

    val lsInfo = Flipped(Valid(new Bundle {
      val addr         = Input(UInt(XLEN.W))
      val wdata        = Input(UInt(XLEN.W))
      val wstrb        = Input(UInt((XLEN / 8).W))
    }))

    val lsuOut = Decoupled(new Bundle {
      val rd          = UInt(5.W)
      val data        = UInt(XLEN.W)
    })

    val rd            = Output(UInt(5.W))
    val data          = Output(UInt(XLEN.W))
  })

  val lsuRes          = Mux(io.lsuIn.bits.lsuOp(R_TAG), 0.U, io.lsuIn.bits.exuRes)

  io.rd               := io.lsuIn.bits.rd
  io.data             := lsuRes

  io.lsuIn.ready      := io.lsuOut.valid

  io.lsuOut.valid     := io.lsuIn.valid && false.B /** TOOD */
  io.lsuOut.bits.rd   := io.lsuIn.bits.rd
  io.lsuOut.bits.data := lsuRes
}
