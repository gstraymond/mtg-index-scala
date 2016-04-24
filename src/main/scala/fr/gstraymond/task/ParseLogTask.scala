package fr.gstraymond.task
import fr.gstraymond.logs.LogParser

import scala.concurrent.Future

object ParseLogTask extends Task[Unit]{
  override def process: Future[Unit] = Future.successful {
    LogParser.parse()
  }
}
