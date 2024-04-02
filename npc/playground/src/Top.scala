import chisel3._
import core.Core
import memory.Sram

class Top extends Module {
  val core    = Module(new Core)
  val sram    = Module(new Sram)

  core.axi <> sram.axi
}
