import mill._
import mill.scalalib._
import mill.scalalib.scalafmt.ScalafmtModule
import mill.scalalib.TestModule.ScalaTest

object playground extends ScalaModule with ScalafmtModule { m =>
  def scalaVersion = "2.13.12"
  def scalacOptions = Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Ywarn-unused",
    "-Ymacro-annotations"
  )

  def ivyDeps = Agg(
    ivy"org.chipsalliance::chisel:6.5.0"
  )
  def scalacPluginIvyDeps = Agg(
    ivy"org.chipsalliance:::chisel-plugin:6.5.0"
  )

  object test extends ScalaTests with ScalaTest {
    def ivyDeps = m.ivyDeps() ++ Agg(
      ivy"org.scalatest::scalatest::3.2.19"
    )
  }
}
