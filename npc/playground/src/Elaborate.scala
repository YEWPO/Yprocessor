import circt.stage._

object Elaborate extends App {
  def top = new Top

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
