import Dependencies._

lazy val commonSettings: Seq[Def.Setting[_]] = {
  Seq(
    organization := "com.stocktrading",
    version := "SNAPSHOT",
    scalaVersion := "2.12.2",
    logLevel := Level.Info,
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases"),
      Resolver.sonatypeRepo("releases")
    ),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:postfixOps",
      "-language:implicitConversions"
    ),
    javaOptions += "-Xmx4G",
    parallelExecution in Test := true,
    fork := true
  )
}

lazy val rootSettings: Seq[Def.Setting[_]] = {
  val settings = Seq(
    name := "stocktrading",
    libraryDependencies ++= Seq(
      scalatest,
      scalamock
    )
  )
  commonSettings ++ settings
}

lazy val root = Project(
  id = "stocktrading",
  base = file("."),
  settings = rootSettings
)
