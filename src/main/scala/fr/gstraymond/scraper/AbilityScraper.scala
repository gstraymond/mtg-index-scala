package fr.gstraymond.scraper

import fr.gstraymond.utils.Log

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AbilityScraper extends WikipediaScraper with Log {

  private val path = "/wiki/List_of_Magic:_The_Gathering_keywords"

  def scrap: Future[Seq[String]] =
    scrap(path).map { doc =>
      val cssPath = "h3 span.mw-headline"
      val abilities = doc.select(cssPath).asScala.map(_.text)
      log.info(abilities.mkString(", "))
      assert(abilities.nonEmpty, s"no result for ${getClass.getSimpleName} [$cssPath]")
      abilities.toSeq
    }
}
