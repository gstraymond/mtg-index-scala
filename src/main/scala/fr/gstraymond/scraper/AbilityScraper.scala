package fr.gstraymond.scraper

import fr.gstraymond.utils.Log

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

object AbilityScraper extends WikipediaScraper with Log:

  private val path = "/wiki/List_of_Magic:_The_Gathering_keywords"

  def scrap: Future[Seq[String]] =
    scrap(path).map { doc =>
      val cssPath   = "div.mw-heading3 h3"
      val abilities = doc.select(cssPath).asScala.map(_.text)
      log.info(abilities.mkString(", "))
      assert(abilities.nonEmpty, s"no result for ${getClass.getSimpleName} [$cssPath]")
      abilities.toSeq
    }
