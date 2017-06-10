package fr.gstraymond.scraper

import fr.gstraymond.utils.Log

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AbilityScraper extends WikipediaScraper with Log {

  private val path = "/wiki/List_of_Magic:_The_Gathering_keywords"

  def scrap: Future[Seq[String]] =
    scrap(path).map { doc =>
      val abilities = doc.select("h3 span.mw-headline").asScala.map(_.text)
      log.info(abilities.mkString(", "))
      abilities
    }
}
