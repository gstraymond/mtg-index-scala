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

  def get(path: String, followRedirect: Boolean = false): Future[Document] = {
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
