import org.typelevel.sbt.tpolecat.DevMode

import Dependencies.*

ThisBuild / scalaVersion                 := "3.5.0"
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
    scalacOptions ++= Seq(
      // https://docs.scala-lang.org/scala3/reference/other-new-features/indentation.html
      "-no-indent",
      "-rewrite",
      // https://docs.scala-lang.org/scala3/reference/other-new-features/safe-initialization.html
      "-Wsafe-init",
      // https://docs.scala-lang.org/scala3/guides/migration/tooling-migration-mode.html
      "-source:3.0-migration"
    )
  )
  .dependsOn(macros)

// TODO can be merged in root
lazy val macros = project.settings(
  libraryDependencies += jsoniter_macros % "compile-internal"
)
