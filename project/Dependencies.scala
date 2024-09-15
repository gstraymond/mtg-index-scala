import sbt.*

object Dependencies {
  lazy val sttp = "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M17"
  lazy val jsoup    = "org.jsoup"        % "jsoup"              % "1.13.+"
  lazy val scribe  = "com.outr" %% "scribe" % "3.15.0"

  lazy val jsoniter_core   = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.30.7"
  lazy val jsoniter_macros = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.7"

  lazy val specs2_core  = "org.specs2" %% "specs2-core"  % "5.5.3"
  lazy val specs2_junit = "org.specs2" %% "specs2-junit" % "5.5.3"
}
