package fr.gstraymond.scraper

import fr.gstraymond.utils.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import sttp.client4.Request
import sttp.client4.httpclient.HttpClientFutureBackend
import sttp.client4.quick.*
import sttp.model.MediaType.ApplicationJson

import java.nio.charset.StandardCharsets.UTF_8
import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import sttp.model.HeaderNames.UserAgent

trait Scraper extends Log {
  def host: String

  val TIMEOUT: Int = 60 * 1000

  val protocol = "https"

  def buildFullUrl(path: String): String = s"$protocol://$host$path"

  def oldScrap(path: String): Future[Document] = {
    val fullUrl = buildFullUrl(path)
    Future {
      val now = new Date().getTime
      now -> Jsoup.connect(fullUrl).timeout(TIMEOUT).get()
    }.map { case (now, doc) =>
      log.info(s"scraping url $fullUrl done in ${new Date().getTime - now}ms !")
      doc
    }
  }

  def scrap(path: String, followRedirect: Boolean = false): Future[Document] = {
    val fullUrl = buildFullUrl(path)

    Sttp.getString(fullUrl).map {
      log.info(s"scraping url $fullUrl done")
      Jsoup.parse
    }
  }

  def get(path: String): Future[Array[Byte]] =
    download(buildFullUrl(path))

  def download(fullUrl: String): Future[Array[Byte]] =
    Sttp
      .getBytes(fullUrl)
      .map { bytes =>
        log.info(s"scraping url $fullUrl done")
        bytes
      }
      .recover { case e: Exception =>
        log.warn(s"not found: [${e.getMessage}], $fullUrl")
        Array()
      }
}

trait MTGSalvationScraper extends Scraper {
  override val host     = "mtgsalvation.gamepedia.com"
  override val protocol = "http"
}

trait GathererScraper extends Scraper {
  override val host = "gatherer-static.wizards.com"
}

trait ScryfallScraper extends Scraper {
  override val host = "cards.scryfall.io"
}

trait MtgJsonScraper extends Scraper {
  override val host = "mtgjson.com"
}

trait WikipediaScraper extends Scraper {
  override val host = "en.wikipedia.org"
}

trait WizardsScraper extends Scraper {
  override val host = "magic.wizards.com"
}

object Sttp {
  private val backend = HttpClientFutureBackend()

  private val requestWithUserAgent =
    basicRequest.header(UserAgent, "'MtgSearch/1.0 (https://mtg-search.com; magic.card.search@gmail.com)'")

  def delete(path: String): Future[String] =
    send(quickRequest.delete(uri"""$path"""))

  def getString(path: String): Future[String] =
    send(requestWithUserAgent.get(uri"""$path""")).flatMap {
      case Left(error)   => Future.failed(new RuntimeException(error))
      case Right(string) => Future.successful(string)
    }

  def getBytes(path: String): Future[Array[Byte]] =
    send(basicRequest.get(uri"""$path""").response(asByteArray)).flatMap {
      case Left(error)  => Future.failed(new RuntimeException(error))
      case Right(bytes) => Future.successful(bytes)
    }

  def postJson(path: String, body: String): Future[String] =
    send(
      quickRequest
        .post(uri"""$path""")
        .contentType(ApplicationJson.charset(UTF_8))
        .body(body)
    )

  def putJson(path: String, body: String): Future[String] =
    send(
      quickRequest
        .put(uri"""$path""")
        .contentType(ApplicationJson.charset(UTF_8))
        .body(body)
    )

  private def send[A](req: Request[A]): Future[A] =
    req.send(backend).map(_.body)
}
