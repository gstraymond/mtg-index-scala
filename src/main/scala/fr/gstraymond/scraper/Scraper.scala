package fr.gstraymond.scraper

import java.util.Date

import fr.gstraymond.utils.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import sys.process._

trait Scraper extends Log {
  def host: String

  val TIMEOUT: Int = 20 * 1000

  def get(path: String): Future[Document] = {
    download(path) { p =>
      Jsoup.connect(p).timeout(TIMEOUT).get()
    }
  }

  def curl(path: String): Future[Document] = {
    download(path) { p =>
      Jsoup.parse(s"curl -s -L $p".!!)
    }
  }

  private def download(path: String)(dl: String => Document): Future[Document] = {
    val url = s"http://$host$path"
    Future {
      val now = new Date().getTime
      now -> dl(url)
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

trait MTGSalvationScraper extends Scraper {
  override val host = "mtgsalvation.gamepedia.com"
}