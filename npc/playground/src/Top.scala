import chisel3._

class Top extends Module {
  val io = IO(new Bundle {
    val a = Input(Bool())
    val b = Input(Bool())

    val c = Output(Bool())
  })

  io.c := io.a ^ io.b
}
