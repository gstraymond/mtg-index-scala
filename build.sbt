import Dependencies._

ThisBuild / scalaVersion                 := "3.1.0"
ThisBuild / version                      := "5"
ThisBuild / organization                 := "fr.gstraymond"
ThisBuild / organizationName             := "gstraymond"
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
      dispatch
    ),
    libraryDependencies ++= List(specs2_core, specs2_junit).map(_ % Test),
    Compile / run / mainClass        := Some("fr.gstraymond.task.MtgIndexScala"),
    Compile / packageBin / mainClass := Some("fr.gstraymond.task.MtgIndexScala"),
    scalacOptions ++= Seq("-indent", "-rewrite")
  )
  .dependsOn(macros)

// Scala 2.13 macros compat
lazy val macros = project.settings(
  scalaVersion                          := "2.13.7",
  libraryDependencies += jsoniter_macros % "compile-internal"
)
