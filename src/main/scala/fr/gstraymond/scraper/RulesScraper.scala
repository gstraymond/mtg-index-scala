package fr.gstraymond.scraper

import java.net.URLDecoder

import fr.gstraymond.utils.Log

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

object RulesScraper extends WizardsScraper with Log {

  val path = "/en/game-info/gameplay/rules-and-formats/rules"

  def scrap: Future[(String, Seq[String])] = for {
    doc <- scrap(path)
    rulesTxt = doc.select("div#comprehensive-rules span.txt a").asScala.head.attr("href")
    _ = log.info(s"scrap: $path -> $rulesTxt")
    bytes <- download(rulesTxt)
  } yield {
    val url = URLDecoder.decode(rulesTxt, "utf-8")
    url.split("/").last.split("\\.").head -> Source.fromBytes(bytes)("utf-8").getLines().toSeq
  }
}
