package core

import chisel3._
import chisel3.util.Decoupled
import bus.Axi4Bundle
import core.stages.Ifu
import core.stages.Idu
import core.stages.Exu
import core.stages.Lsu
import core.stages.Wbu
import core.modules.Gpr
import bus.AxiArbiter

class PipeReg[T <: Data](gen: T) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(gen))
    val out = Decoupled(gen)
  })

  val reg = Reg(gen)
  val valid = RegInit(false.B)

  io.in.ready := io.out.ready
  io.out.valid := valid
  io.out.bits := reg

  when(io.in.fire) {
    reg := io.in.bits
    valid := true.B
  }

  when(io.out.fire) {
    valid := false.B
  }
}

class Core extends Module {
  val axi = IO(new Axi4Bundle)

  val ifu = Module(new Ifu)
  val idu = Module(new Idu)
  val exu = Module(new Exu)
  val lsu = Module(new Lsu)
  val wbu = Module(new Wbu)

  val gpr = Module(new Gpr)

  val axiArbiter = Module(new AxiArbiter)

  val ifu2idu = Module(new PipeReg(ifu.io.ifuOut.bits.cloneType))
  val idu2exu = Module(new PipeReg(idu.io.iduOut.bits.cloneType))
  val exu2lsu = Module(new PipeReg(exu.io.exuOut.bits.cloneType))
  val lsu2wbu = Module(new PipeReg(lsu.io.lsuOut.bits.cloneType))

  val idu2gpr = Module(new PipeReg(idu.io.iduGpr.bits.cloneType))

  ifu2idu.io.in <> ifu.io.ifuOut
  idu2exu.io.in <> idu.io.iduOut
  exu2lsu.io.in <> exu.io.exuOut
  lsu2wbu.io.in <> lsu.io.lsuOut

  idu2gpr.io.in <> idu.io.iduGpr

  idu.io.iduIn <> ifu2idu.io.out
  exu.io.exuIn <> idu2exu.io.out
  lsu.io.lsuIn <> exu2lsu.io.out
  wbu.io.wbuIn <> lsu2wbu.io.out

  gpr.io.rsIn       <> idu2gpr.io.out
  gpr.io.rdLsu      := lsu.io.rd
  gpr.io.rdWbu      := wbu.io.rd
  gpr.io.dataLsu    := lsu.io.data
  gpr.io.dataWbu    := wbu.io.data

  ifu.io.control    := exu.io.control
  ifu.io.dnpc       := exu.io.dnpc
  ifu.io.abort      := exu.io.control

  idu.io.abort      := exu.io.control

  exu.io.src1       := gpr.io.src1
  exu.io.src2       := gpr.io.src2

  lsu.io.lsInfo     <> exu.io.lsInfo

  axi            <> axiArbiter.io.axi
  axiArbiter.io.ifu <> ifu.io.axi
  axiArbiter.io.lsu <> lsu.io.axi
}
