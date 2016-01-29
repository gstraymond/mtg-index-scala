package fr.gstraymond.scraper

import fr.gstraymond.utils.Log
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Scraper extends Log {
  val host = "magiccards.info"

  def get(url: String) = Future {
    log.info(s"scraping url $url...")
    Jsoup.connect(url).get()
  }.map { doc =>
    log.info(s"scraping url $url done !")
    doc
  }
}
