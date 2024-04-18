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

object LsState extends ChiselEnum {
  val sIdle, sWork = Value
}

class Ls extends Module {
  val io = IO(new Bundle {
    val en = Input(Bool())

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
  val axiOp       = !memOp || !readOp

  val cacheOpReg  = Reg(Bool())
  val axiOpReg    = Reg(Bool())

  import LsState._
  val stateReg    = RegInit(sIdle)
  val nextState   = WireDefault(sIdle)
  stateReg        := nextState

  when (stateReg === sIdle) {
    cacheOpReg  := cacheOp
    axiOpReg    := axiOp
  }

  when (stateReg === sWork) {
    cacheOpReg  := cacheOpReg
    axiOpReg    := axiOpReg
  }

  switch (stateReg) {
    is (sIdle) {
      when (io.en) {
        nextState := sWork
      }
    }

    is (sWork) {
      nextState := Mux(!cacheOpReg && !axiOpReg, sIdle, sWork)
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
