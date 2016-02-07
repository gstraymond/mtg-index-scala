package fr.gstraymond.scraper

import java.text.SimpleDateFormat
import java.util.Date

import fr.gstraymond.model.ScrapedEdition
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ReleaseDateScraper extends MTGPriceScraper {

  val path = "/magic-the-gathering-prices.jsp"

  val dateFormat = new SimpleDateFormat("MM/dd/yyyy")

  def scrap(editions: Seq[ScrapedEdition]): Future[Seq[ScrapedEdition]] = {
    get(path).map { doc =>
      log.info(s"scraping doc ${doc.title()}")
      extractReleaseDate(doc)
    }.map { result =>
      merge(editions, result.toMap)
    }
  }

  private def extractReleaseDate(doc: Document) = {
    doc.select("#setTable tr").asScala.tail.map { tr =>
      tr.select("td").asScala match {
        case Seq(name, releaseDate) =>
          //log.debug(s"extractReleaseDate name $name releaseDAte $releaseDate")
          val scrapedReleaseDate = dateFormat.parse(releaseDate.text())
          name.text() -> scrapedReleaseDate
      }
    }
  }


  private def merge(editions: Seq[ScrapedEdition], result: Map[String, Date]) = {
    editions.map { edition =>
      result.get(edition.name) match {
        case Some(releaseDate) => edition.copy(releaseDate = Some(releaseDate))
        case _ => edition
      }
    }
  }
}
