package fr.gstraymond.scraper

import java.util.Date

import fr.gstraymond.utils.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Scraper extends Log {
  def host: String

  def get(path: String): Future[Document] = {
    val url = s"http://$host$path"
    Future {
      val now = new Date().getTime
      log.info(s"scraping url $url...")
      now -> Jsoup.connect(url).timeout(20 * 1000).get()
    }.map { case (now, doc) =>
      log.info(s"scraping url $url done in ${new Date().getTime - now}ms !")
      doc
    }
  }
}

trait MagicCardsInfoScraper extends Scraper {
  override val host = "magiccards.info"
}

trait MTGPriceScraper extends Scraper {
  override val host = "www.mtgprice.com"
}

trait MTGGoldFishScraper extends Scraper {
  override val host = "www.mtggoldfish.com"
}