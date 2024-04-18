package core.stages

import chisel3._
import core.CoreConfig._
import bus.Axi4ReadAddrBundle
import chisel3.util.Decoupled
import bus.Axi4ReadDataBundle
import core.modules.Cache
import core.modules.PcGen
import chisel3.util.Cat

class Ifu extends Module {
  val io = IO(new Bundle {
    val ifuOut    = Decoupled(new Bundle {
      val inst  = UInt(32.W)
      val pc    = UInt(XLEN.W)
      val snpc  = UInt(XLEN.W)
    })
    val dnpc      = Input(UInt(XLEN.W))
    val control   = Input(Bool())
    val abort     = Input(Bool())

    val axi       = new Bundle {
      val ar = Decoupled(new Axi4ReadAddrBundle)
      val r  = Flipped(Decoupled(new Axi4ReadDataBundle))
    }
  })

  val icache  = Module(new Cache)
  val pcGen   = Module(new PcGen)

  val pcReg   = RegInit(START_ADDR.U)
  pcReg       := pcGen.io.npc

  val inst    = Mux(icache.io.response.valid, VecInit.tabulate(2){ i => icache.io.response.bits.data(32 * (i + 1) - 1, 32 * i) }(pcReg(2)), 0.U)

  icache.io.request.valid           := !reset.asBool && !io.abort
  icache.io.request.bits.addr       := Cat(pcGen.io.npc(XLEN - 1, 3), 0.U(3.W))
  icache.io.abort                   := io.abort

  pcGen.io.pc                       := pcReg
  pcGen.io.instLen                  := Mux(io.ifuOut.fire, inst(1, 0), 0.U)
  pcGen.io.dnpc                     := io.dnpc
  pcGen.io.control                  := io.control

  io.axi                            <> icache.io.axi

  io.ifuOut.valid                   := icache.io.response.valid && !io.abort
  io.ifuOut.bits.inst               := inst
  io.ifuOut.bits.pc                 := pcReg
  io.ifuOut.bits.snpc               := pcGen.io.snpc
}
