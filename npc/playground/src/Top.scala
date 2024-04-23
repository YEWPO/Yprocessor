import chisel3._
import core.Core
import memory.Peri

class Top extends Module {
  val core    = Module(new Core)
  val peri    = Module(new Peri)

  core.axi <> peri.axi
}
