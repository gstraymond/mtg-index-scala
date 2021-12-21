import sbt._

object Dependencies {
  lazy val dispatch = "org.dispatchhttp" %% "dispatch-core"   % "1.2.0"
  lazy val jsoup    = "org.jsoup"         % "jsoup"           % "1.13.+"
  lazy val logback  = "ch.qos.logback"    % "logback-classic" % "1.2.+"
  lazy val slf4j    = "org.slf4j"         % "slf4j-api"       % "1.7.+"

  lazy val jsoniter_core   = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.6.4"
  lazy val jsoniter_macros = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.6.4"

  lazy val junit        = "junit"       % "junit"        % "4.+"
  lazy val specs2_core  = "org.specs2" %% "specs2-core"  % "4.+"
  lazy val specs2_junit = "org.specs2" %% "specs2-junit" % "4.+"
}
