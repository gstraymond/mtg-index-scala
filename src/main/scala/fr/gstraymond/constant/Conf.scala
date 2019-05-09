package fr.gstraymond.constant

import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig

object Conf {

  def coloredLogs: Boolean = Option(System.getenv("LOG_COLOR")).isDefined
}

object JsonConf {
  val codecMakerConfig = CodecMakerConfig(transientEmpty = false)
}
