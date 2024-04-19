package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._
import LsuOp._
import bus.Axi4Bundle
import core.modules.SufLsu
import memory.MemoryConfig._
import chisel3.util.Cat
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.MuxCase
import bus.Axi4ReadAddrBundle
import bus.Axi4WriteAddrBundle
import bus.Axi4WriteDataBundle
import core.modules.DCache

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

object AxiLsState extends ChiselEnum {
  val sIdle, sAR, sR, sAW, sW, sB = Value
}

class AxiLs extends Module {
  val io = IO(new Bundle {
    val en      = Input(Bool())

    val addr    = Input(UInt(XLEN.W))
    val wdata   = Input(UInt(XLEN.W))
    val strb    = Input(UInt((XLEN / 8).W))

    val rdata   = Output(UInt(XLEN.W))
    val done    = Output(Bool())

    val axi     = new Axi4Bundle
  })

  import AxiLsState._
  val stateReg    = RegInit(sIdle)
  val nextState   = WireDefault(sIdle)
  stateReg        := nextState

  io.axi.ar.valid := stateReg === sAR
  io.axi.ar.bits  := Axi4ReadAddrBundle(
    io.addr,
    0.U,
    3.U
  )
  io.axi.r.ready  := stateReg === sR

  io.axi.aw.valid := stateReg === sAW
  io.axi.aw.bits  := Axi4WriteAddrBundle(
    io.addr,
    0.U,
    3.U
  )
  io.axi.w.valid  := stateReg === sW
  io.axi.w.bits   := Axi4WriteDataBundle(
    io.wdata,
    io.strb,
    true.B
  )
  io.axi.b.ready  := stateReg === sB

  io.rdata        := io.axi.r.bits.data
  io.done         := io.axi.b.fire || io.axi.r.fire

  switch (stateReg) {
    is (sIdle) {
      nextState := MuxCase(sIdle, Seq(
        (io.en && !io.strb.orR) -> sAR,
        (io.en && io.strb.orR)  -> sAW
      ))
    }

    is (sAR) {
      nextState := Mux(io.axi.ar.fire, sR, sAR)
    }

    is (sR) {
      nextState := Mux(io.axi.r.fire, sIdle, sR)
    }

    is (sAW) {
      nextState := Mux(io.axi.aw.fire, sW, sAW)
    }

    is (sW) {
      nextState := Mux(io.axi.w.fire, sB, sW)
    }

    is (sB) {
      nextState := Mux(io.axi.b.fire, sIdle, sB)
    }
  }
}

object LsState extends ChiselEnum {
  val sIdle, sWork = Value
}

class Ls extends Module {
  val io = IO(new Bundle {
    val en      = Input(Bool())

    val addr    = Input(UInt(XLEN.W))
    val wdata   = Input(UInt(XLEN.W))
    val strb    = Input(UInt((XLEN / 8).W))

    val rdata   = Output(UInt(XLEN.W))
    val done    = Output(Bool())

    val axi     = new Axi4Bundle
  })

  val memOp       = io.en && (io.addr >= MEM_ADDR_BASE.U) && (io.addr < MEM_ADDR_MAX.U)
  val readOp      = io.en && !io.strb.orR
  val cacheOp     = memOp
  val axiOp       = io.en && (!memOp || !readOp)

  val cacheOpReg  = Reg(Bool())
  val axiOpReg    = Reg(Bool())

  import LsState._
  val stateReg    = RegInit(sIdle)
  val nextState   = WireDefault(sIdle)
  stateReg        := nextState

  val axiLs       = Module(new AxiLs)
  val dcache      = Module(new DCache)

  axiLs.io.en     := axiOpReg
  axiLs.io.addr   := io.addr
  axiLs.io.wdata  := io.wdata
  axiLs.io.strb   := io.strb

  dcache.io.request.valid := cacheOpReg
  dcache.io.request.bits.addr := io.addr
  dcache.io.request.bits.data := io.wdata
  dcache.io.request.bits.strb := io.strb

  io.axi.ar.valid := Mux(axiOpReg, axiLs.io.axi.ar.valid, dcache.io.axi.ar.valid)
  io.axi.ar.bits  := Mux(axiOpReg, axiLs.io.axi.ar.bits, dcache.io.axi.ar.bits)
  io.axi.r.ready  := Mux(axiOpReg, axiLs.io.axi.r.ready, dcache.io.axi.r.ready)

  axiLs.io.axi.ar.ready := Mux(axiOpReg, io.axi.ar.ready, false.B)
  axiLs.io.axi.r.valid  := Mux(axiOpReg, io.axi.r.valid, false.B)
  axiLs.io.axi.r.bits   := io.axi.r.bits

  dcache.io.axi.ar.ready := Mux(cacheOpReg, io.axi.ar.ready, false.B)
  dcache.io.axi.r.valid  := Mux(cacheOpReg, io.axi.r.valid, false.B)
  dcache.io.axi.r.bits   := io.axi.r.bits

  io.axi.aw        <> axiLs.io.axi.aw
  io.axi.w         <> axiLs.io.axi.w
  io.axi.b         <> axiLs.io.axi.b

  io.rdata        := Mux(axiOpReg, axiLs.io.rdata, dcache.io.response.bits.data)
  io.done         := !(axiOpReg && !axiLs.io.done) && !(cacheOpReg && !dcache.io.response.valid)

  when (stateReg === sIdle) {
    cacheOpReg  := cacheOp
    axiOpReg    := axiOp
  }

  when (stateReg === sWork) {
    cacheOpReg  := cacheOpReg && !dcache.io.response.valid
    axiOpReg    := axiOpReg && !axiLs.io.done
  }

  switch (stateReg) {
    is (sIdle) {
      when (io.en) {
        nextState := sWork
      }
    }

    is (sWork) {
      nextState := Mux(io.done, sIdle, sWork)
    }
  }
}

class Lsu extends Module {
  val io = IO(new Bundle {
    val lsuIn = Flipped(Decoupled(new Bundle {
      val lsInfo      = new Bundle {
        val wdata      = UInt(XLEN.W)
        val wstrb      = UInt((XLEN / 8).W)
      }

      val rd          = UInt(GPR_LEN.W)
      val exuRes      = UInt(XLEN.W)
      val lsuOp       = UInt(lsuOpLen.W)
      val kill        = Bool()
      val invalid     = Bool()
      val inst        = UInt(32.W)
      val dnpc        = UInt(XLEN.W)
    }))

    val lsuOut = Decoupled(new Bundle {
      val rd          = UInt(GPR_LEN.W)
      val lsuRes      = UInt(XLEN.W)
      val kill        = Bool()
      val invalid     = Bool()
      val inst        = UInt(32.W)
      val dnpc        = UInt(XLEN.W)
    })

    val axi               = new Axi4Bundle

    val rd                = Output(UInt(GPR_LEN.W))
    val data              = Output(UInt(XLEN.W))
  })

  val sufLsu              = Module(new SufLsu)

  val lsModule            = Module(new Ls)

  lsModule.io.en          := Mux(io.lsuIn.valid, io.lsuIn.bits.lsuOp(R_TAG) || io.lsuIn.bits.lsuOp(W_TAG), false.B)
  lsModule.io.addr        := Cat(io.lsuIn.bits.exuRes(31, 3), 0.U(3.W))
  lsModule.io.wdata       := io.lsuIn.bits.lsInfo.wdata
  lsModule.io.strb        := io.lsuIn.bits.lsInfo.wstrb

  sufLsu.io.lsuOp         := Mux(io.lsuIn.valid, io.lsuIn.bits.lsuOp, 0.U)
  sufLsu.io.addr          := io.lsuIn.bits.exuRes
  sufLsu.io.src           := lsModule.io.rdata

  val lsuRes              = Mux(io.lsuIn.bits.lsuOp(R_TAG), sufLsu.io.data, io.lsuIn.bits.exuRes)
  val lsuFin              = (!io.lsuIn.bits.lsuOp(R_TAG) && !io.lsuIn.bits.lsuOp(W_TAG)) || lsModule.io.done

  io.axi                  <> lsModule.io.axi

  io.rd                   := io.lsuIn.bits.rd
  io.data                 := lsuRes

  io.lsuIn.ready          := io.lsuOut.ready && lsuFin

  io.lsuOut.valid         := io.lsuIn.valid && lsuFin
  io.lsuOut.bits.rd       := io.lsuIn.bits.rd
  io.lsuOut.bits.lsuRes   := lsuRes
  io.lsuOut.bits.kill     := io.lsuIn.bits.kill
  io.lsuOut.bits.invalid  := io.lsuIn.bits.invalid
  io.lsuOut.bits.inst     := io.lsuIn.bits.inst
  io.lsuOut.bits.dnpc     := io.lsuIn.bits.dnpc
}
