import org.typelevel.sbt.tpolecat.DevMode

import Dependencies.*

ThisBuild / scalaVersion                 := "3.4.2"
ThisBuild / version                      := "15"
ThisBuild / organization                 := "fr.gstraymond"
ThisBuild / organizationName             := "gstraymond"
ThisBuild / packageDoc / publishArtifact := false
ThisBuild / packageSrc / publishArtifact := false

ThisBuild / tpolecatDefaultOptionsMode := DevMode

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "mtg-index-scala",
    libraryDependencies ++= List(
      slf4j,
      logback,
      jsoup,
      jsoniter_core,
      sttp
    ),
    libraryDependencies ++= List(specs2_core, specs2_junit).map(_ % Test),
    Compile / run / mainClass        := Some("fr.gstraymond.task.MtgIndexScala"),
    Compile / packageBin / mainClass := Some("fr.gstraymond.task.MtgIndexScala"),
    scalacOptions ++= Seq("-no-indent", "-rewrite")
  )
  .dependsOn(macros)

// TODO can be merged in root
lazy val macros = project.settings(
  libraryDependencies += jsoniter_macros % "compile-internal"
)
