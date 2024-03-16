package core.stages

import chisel3._

object ExuSrc1 {
  val SRC1  = false.B
  val PC    = true.B
}

object ExuSrc2 {
  val IMM   = false.B
  val SRC2  = true.B
}

class Exu extends Module {
}
