package fr.gstraymond.constant

object Conf {

  def coloredLogs: Boolean = Option(System.getenv("LOG_COLOR")).isDefined
}
