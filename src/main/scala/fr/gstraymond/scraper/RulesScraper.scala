package fr.gstraymond.scraper

import fr.gstraymond.utils.Log

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

object RulesScraper extends WizardsScraper with Log {

  val path = "/en/game-info/gameplay/rules-and-formats/rules"

  def scrap: Future[Seq[String]] = for {
    doc <- scrap(path)
    rulesTxt = doc.select("div#comprehensive-rules span.txt a").asScala.head.attr("href")
    _ = log.info(s"scrap: $path -> $rulesTxt")
    bytes <- download(rulesTxt)
  } yield {
    Source.fromBytes(bytes)("CP1252").getLines().toSeq
  }
}
