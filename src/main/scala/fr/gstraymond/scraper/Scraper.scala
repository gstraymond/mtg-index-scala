package fr.gstraymond.scraper

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.utils.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.mutable
import scala.concurrent.Future

trait Scraper extends Log {
  def host: String

  val TIMEOUT: Int = 20 * 1000

  def scrap(path: String, followRedirect: Boolean = false): Future[Document] = {
    val fullUrl = s"http://$host$path"
    val http = followRedirect match {
      case true =>
        val h = Http.configure(_ setFollowRedirect true)
        HttpClients.addClient(h)
      case _ => Http
    }

    http {
      url(fullUrl) OK as.String
    }.map {
      log.info(s"scraping url $fullUrl done")
      Jsoup.parse
    }
  }

  def get(path: String): Future[Array[Byte]] = {
    Http {
      url(s"http://$host$path") OK as.Bytes
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

trait YawgatogScraper extends Scraper {
  override val host = "www.yawgatog.com"
}

object HttpClients {
  private val list = mutable.Buffer[Http]()

  def addClient(http: Http) = {
    list.append(http)
    http
  }

  def shutdown() = {
    list.foreach(_.shutdown())
    Http.shutdown()
  }
}
