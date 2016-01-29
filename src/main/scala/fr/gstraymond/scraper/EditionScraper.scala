package fr.gstraymond.scraper

import fr.gstraymond.model.ScrapedEdition
import fr.gstraymond.utils.Log
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EditionScraper extends Scraper with Log {

  val cardExpression = "table[cellpadding=3] tr"
  val editionExpression = "ul li ul li"
  val editionsPropertiesPath = "src/main/resources/scrap/editions.properties"
  val url = s"http://$host/sitemap.html"

  def scrap: Future[Seq[ScrapedEdition]] = {
    get(url).map { doc =>
      log.info(s"scraping doc ${doc.title()}")
      extractCodeName(extractTable(doc))
    }
  }

  private def extractTable(doc: Document): Seq[Element] = {
    doc.select("table").asScala match {
      case _ +: table +: _ => table.select(editionExpression).asScala
      case _ => throw new scala.Exception(s"No results found for : url '$url' and expression 'table' ")
    }
  }

  private def extractCodeName(elements: Seq[Element]): Seq[ScrapedEdition] = {
    log.info(s"extractCodeName ${elements.size} elements")
    elements match {
      case Seq() => throw new scala.Exception(s"No results found for : url '$url' and expression '$editionExpression' ")
      case elems => elems.map { elem =>
        val code = elem.getElementsByTag("small").first().text()
        val name = elem.getElementsByTag("a").first().text()
        ScrapedEdition(code, name)
      }
    }
  }
}
