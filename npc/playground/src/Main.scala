import circt.stage._
import core.Core

object Main extends App {
  def top = new Core

  val firtoolOptions = Seq(
    FirtoolOption(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays,locationInfoStyle=wrapInAtSquareBracket"
    ),
    FirtoolOption("--split-verilog"),
    FirtoolOption("-o=build/gen"),
    FirtoolOption("--disable-all-randomization")
  )
  val buildOptions = Seq(
    chisel3.stage.ChiselGeneratorAnnotation(() => top),
    CIRCTTargetAnnotation(CIRCTTarget.SystemVerilog)
  )

  val options = firtoolOptions ++ buildOptions

  (new ChiselStage).execute(args, options)
}
