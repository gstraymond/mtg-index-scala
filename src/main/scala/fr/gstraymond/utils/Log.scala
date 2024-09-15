package fr.gstraymond.utils

import scribe.Level
import scribe.Logger
import scribe.format.Formatter

import scala.sys.env

object Log {
  private val formatter: Formatter =
    env.get("USER") match {
      case Some("root") => Formatter.classic
      case _            => Formatter.compact
    }

  val default: Logger =
    Logger.root
      .clearHandlers()
      .clearModifiers()
      .withHandler(formatter = formatter, minimumLevel = Some(Level.Debug))
      .replace()
}

trait Log {
  protected val log: Logger = Log.default
}
