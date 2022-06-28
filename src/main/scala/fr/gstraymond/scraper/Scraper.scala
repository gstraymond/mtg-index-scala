package fr.gstraymond.scraper

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.utils.Log
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.util.Date
import scala.collection.mutable
import scala.concurrent.Future

trait Scraper extends Log:
  def host: String

  val TIMEOUT: Int = 60 * 1000

  val protocol = "https"

  def buildFullUrl(path: String): String = s"$protocol://$host$path"

  def oldScrap(path: String): Future[Document] =
    val fullUrl = buildFullUrl(path)
    Future {
      val now = new Date().getTime
      now -> Jsoup.connect(fullUrl).timeout(TIMEOUT).get()
    }.map { case (now, doc) =>
      log.info(s"scraping url $fullUrl done in ${new Date().getTime - now}ms !")
      doc
    }

  def scrap(path: String, followRedirect: Boolean = false): Future[Document] =
    val fullUrl = buildFullUrl(path)
    val http = followRedirect match
      case true =>
        val h = Http.withConfiguration(_ setFollowRedirect true)
        HttpClients.addClient(h)
      case _ => Http.default

    http {
      url(fullUrl) OK as.String
    }.map {
      log.info(s"scraping url $fullUrl done")
      Jsoup.parse
    }

  def get(path: String, disableSslValidation: Boolean = false): Future[Array[Byte]] = 
    download(buildFullUrl(path), disableSslValidation)

  private val insecureSslContext = SslContextBuilder
    .forClient()
    .sslProvider(SslProvider.JDK)
    .trustManager(InsecureTrustManagerFactory.INSTANCE)
    .build()

  def download(fullUrl: String, disableSslValidation: Boolean = false): Future[Array[Byte]] =
    (disableSslValidation match {
      case true  => Http.withConfiguration(_.setSslContext(insecureSslContext))
      case false => Http.default
    }) {
      url(fullUrl) OK as.Bytes
    }
      .map { bytes =>
        log.info(s"scraping url $fullUrl done")
        bytes
      }
      .recover { case e: Exception =>
        log.warn(s"not found: [${e.getMessage}], $fullUrl")
        Array()
      }

trait MTGSalvationScraper extends Scraper:
  override val host     = "mtgsalvation.gamepedia.com"
  override val protocol = "http"

trait GathererScraper extends Scraper:
  override val host = "gatherer.wizards.com"

trait MtgJsonScraper extends Scraper:
  override val host = "mtgjson.com"

trait WikipediaScraper extends Scraper:
  override val host = "en.wikipedia.org"

trait WizardsScraper extends Scraper:
  override val host = "magic.wizards.com"

object HttpClients:
  private val list = mutable.Buffer[Http]()

  def addClient(http: Http) =
    list.append(http)
    http

  def shutdown() =
    list.foreach(_.shutdown())
    Http.default.shutdown()
