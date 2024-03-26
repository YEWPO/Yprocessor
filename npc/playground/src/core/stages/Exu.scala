package core.stages

import chisel3._
import chisel3.util.Decoupled
import core.CoreConfig._
import core.modules.AluOp._
import core.modules.BuOp._
import LsuOp._
import core.modules.Alu
import core.modules.Bu
import core.modules.PreLsu
import chisel3.util.Cat

object ExuSrc1 {
  val SRC1  = false.B
  val PC    = true.B
}

object ExuSrc2 {
  val IMM   = false.B
  val SRC2  = true.B
}

class Exu extends Module {
  val io = IO(new Bundle {
    val exuIn = Flipped(Decoupled(new Bundle {
      val rd          = UInt(5.W)
      val imm         = UInt(XLEN.W)
      val src1Sel     = Bool()
      val src2Sel     = Bool()
      val aluOp       = UInt(aluOpLen.W)
      val buOp        = UInt(buOpLen.W)
      val lsuOp       = UInt(lsuOpLen.W)
      val kill        = Bool()
      val invalid     = Bool()
      val pc          = UInt(XLEN.W)
      val snpc        = UInt(XLEN.W)
    }))

    val src1          = Input(UInt(XLEN.W))
    val src2          = Input(UInt(XLEN.W))

    val dnpc          = Output(UInt(XLEN.W))
    val control       = Output(Bool())

    val addr         = Output(UInt(XLEN.W))
    val wdata         = Output(UInt(XLEN.W))
    val wstrb         = Output(UInt((XLEN / 8).W))

    val exuOut = Decoupled(new Bundle {
      val rd          = UInt(5.W)
      val exuRes      = UInt(XLEN.W)
      val lsuOp       = UInt(lsuOpLen.W)
      val kill        = Bool()
      val invalid     = Bool()
      val pc          = UInt(XLEN.W)
    })
  })

  val alu                 = Module(new Alu)
  val bu                  = Module(new Bu)
  val preLsu              = Module(new PreLsu)

  alu.io.aluOp            := Mux(io.exuIn.valid, io.exuIn.bits.aluOp, 0.U)
  alu.io.src1             := Mux(io.exuIn.bits.src1Sel, io.exuIn.bits.pc, io.src1)
  alu.io.src2             := Mux(io.exuIn.bits.src2Sel, io.src2, io.exuIn.bits.imm)

  bu.io.buOp              := Mux(io.exuIn.valid, io.exuIn.bits.buOp, 0.U)
  bu.io.src1              := io.src1
  bu.io.src2              := io.src2
  bu.io.tpc               := alu.io.res

  preLsu.io.lsuOp         := Mux(io.exuIn.valid, io.exuIn.bits.lsuOp, 0.U)
  preLsu.io.addr          := alu.io.res
  preLsu.io.src           := io.src2

  io.exuIn.ready          := io.exuOut.ready

  io.exuOut.valid         := io.exuIn.valid
  io.exuOut.bits.rd       := Mux(io.exuIn.valid, io.exuIn.bits.rd, 0.U)
  io.exuOut.bits.exuRes   := Mux(io.exuIn.bits.lsuOp(lsuOpLen - 1), io.exuIn.bits.snpc, alu.io.res)
  io.exuOut.bits.lsuOp    := Mux(io.exuIn.valid, io.exuIn.bits.lsuOp, 0.U)
  io.exuOut.bits.kill     := Mux(io.exuIn.valid, io.exuIn.bits.kill, false.B)
  io.exuOut.bits.invalid  := Mux(io.exuIn.valid, io.exuIn.bits.invalid, false.B)
  io.exuOut.bits.pc       := Mux(io.exuIn.valid, io.exuIn.bits.pc, 0.U)

  io.addr                 := Cat(alu.io.res(XLEN - 1, 3), 0.U(3.W))
  io.wdata                := preLsu.io.data
  io.wstrb                := preLsu.io.strb

  io.dnpc                 := bu.io.dnpc
  io.control              := bu.io.control
}
