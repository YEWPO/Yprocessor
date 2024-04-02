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

  io.in.ready := !valid
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
  val io = IO(new Bundle {
    val axi = new Axi4Bundle
  })

  val ifu = Module(new Ifu)
  val idu = Module(new Idu)
  val exu = Module(new Exu)
  val lsu = Module(new Lsu)
  val wbu = Module(new Wbu)

  val gpr = Module(new Gpr)

  val axiArbiter = Module(new AxiArbiter)

  val ifu2idu = Module(new PipeReg(ifu.io.ifuOut))
  val idu2exu = Module(new PipeReg(idu.io.iduOut))
  val exu2lsu = Module(new PipeReg(exu.io.exuOut))
  val lsu2wbu = Module(new PipeReg(lsu.io.lsuOut))

  idu.io.iduIn <> ifu2idu.io.out
  exu.io.exuIn <> idu2exu.io.out
  lsu.io.lsuIn <> exu2lsu.io.out
  wbu.io.wbuIn <> lsu2wbu.io.out

  io.axi <> axiArbiter.io.axi
}
