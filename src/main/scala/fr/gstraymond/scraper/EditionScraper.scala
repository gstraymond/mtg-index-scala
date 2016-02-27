package fr.gstraymond.scraper

import fr.gstraymond.model.ScrapedEdition
import fr.gstraymond.utils.Log
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EditionScraper extends MagicCardsInfoScraper with Log {

  val editionExpression = "ul li ul li"
  val path = "/sitemap.html"

  def scrap: Future[Seq[ScrapedEdition]] = {
    scrap(path).map { doc =>
      log.info(s"scraping doc ${doc.title()}")
      extractTable(doc)
    }.map {
      extractCodeName
    }
  }

  private def extractTable(doc: Document): Seq[Element] = {
    doc.select("table").asScala match {
      case _ +: table +: _ => table.select(editionExpression).asScala
      case _ => throw new scala.Exception(s"No results found for : url '$path' and expression 'table' ")
    }
  }

  private def extractCodeName(elements: Seq[Element]): Seq[ScrapedEdition] = {
    log.info(s"extractCodeName ${elements.size} elements")
    elements match {
      case Seq() => throw new scala.Exception(s"No results found for : url '$path' and expression '$editionExpression' ")
      case elems => elems.map { elem =>
        val code = elem.getElementsByTag("small").first().text()
        val name = elem.getElementsByTag("a").first().text()
        ScrapedEdition(code, name, None, None)
      }
    }
  }
}
