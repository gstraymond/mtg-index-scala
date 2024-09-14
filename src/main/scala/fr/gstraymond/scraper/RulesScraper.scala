package fr.gstraymond.scraper

import fr.gstraymond.utils.Log

import java.net.URLDecoder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.jdk.CollectionConverters.*

object RulesScraper extends WizardsScraper with Log {

  val path = "/en/rules"

  def scrap: Future[(String, Seq[String])] = for {
    doc <- scrap(path)
    rulesTxt = doc.select("p span.txt a.cta").asScala.head.attr("href")
    _        = log.info(s"scrap: $path -> $rulesTxt")
    bytes <- download(rulesTxt.replace(" ", "%20"))
  }
  yield {
    val url = URLDecoder.decode(rulesTxt, "utf-8")
    url.split("/").last.split("\\.").head -> Source.fromBytes(bytes)("utf-8").getLines().toSeq
  }
}
