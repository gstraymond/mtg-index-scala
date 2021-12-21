import Dependencies._

ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "1"
ThisBuild / organization := "fr.gstraymond"
ThisBuild / organizationName := "gstraymond"
ThisBuild / packageDoc / publishArtifact := false
ThisBuild / packageSrc / publishArtifact := false

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "mtg-index-scala",
    libraryDependencies ++= List(
      slf4j,
      logback,
      jsoup,
      jsoniter_core,
      jsoniter_macros % "compile-internal",
      dispatch
    ),
    libraryDependencies ++= List(specs2_core, specs2_junit, junit).map(_ % Test),
    Compile / run / mainClass := Some("fr.gstraymond.task.DEALTask"),
    Compile / packageBin / mainClass := Some("fr.gstraymond.task.DEALTask")
  )
