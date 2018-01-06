package fr.gstraymond.scraper

import java.util.Date

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.utils.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.mutable
import scala.concurrent.Future

trait Scraper extends Log {
  def host: String

  val TIMEOUT: Int = 60 * 1000

  val protocol = "https"

  def oldScrap(path: String): Future[Document] = {
    val fullUrl = s"$protocol://$host$path"
    Future {
      val now = new Date().getTime
      now -> Jsoup.connect(fullUrl).timeout(TIMEOUT).get()
    }.map { case (now, doc) =>
      log.info(s"scraping url $fullUrl done in ${new Date().getTime - now}ms !")
      doc
    }
  }

  def scrap(path: String, followRedirect: Boolean = false): Future[Document] = {
    val fullUrl = s"$protocol://$host$path"
    val http = followRedirect match {
      case true =>
        val h = Http.withConfiguration(_ setFollowRedirect true)
        HttpClients.addClient(h)
      case _ => Http.default
    }

    http {
      url(fullUrl) OK as.String
    }.map {
      log.info(s"scraping url $fullUrl done")
      Jsoup.parse
    }
  }

  def get(path: String): Future[Array[Byte]] = download(s"$protocol://$host$path")

  def download(fullUrl: String): Future[Array[Byte]] = {
    Http.default {
      url(fullUrl) OK as.Bytes
    }.map { bytes =>
      log.info(s"scraping url $fullUrl done")
      bytes
    }.recover {
      case e: Exception =>
        log.warn(s"not found: [${e.getMessage}], $fullUrl")
        Array()
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
  override val protocol = "http"
}

trait YawgatogScraper extends Scraper {
  override val host = "www.yawgatog.com"
}

trait GathererScraper extends Scraper {
  override val host = "gatherer.wizards.com"
}

trait MtgJsonScraper extends Scraper {
  override val host = "mtgjson.com"
}

trait WikipediaScraper extends Scraper {
  override val host = "en.wikipedia.org"
}

trait WizardsScraper extends Scraper {
  override val host: String = "magic.wizards.com"
}

object HttpClients {
  private val list = mutable.Buffer[Http]()

  def addClient(http: Http) = {
    list.append(http)
    http
  }

  def shutdown() = {
    list.foreach(_.shutdown())
    Http.default.shutdown()
  }
}
