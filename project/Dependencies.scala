import sbt._

object Dependencies {
  val scalatestVersion = "3.0.1"
  val scalamockVersion = "3.5.0"

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % Test
  val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % scalamockVersion % Test
}