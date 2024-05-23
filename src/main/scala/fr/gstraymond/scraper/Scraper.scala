package fr.gstraymond.scraper

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.utils.Log
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.util.Date
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

    (followRedirect match
      case true => HttpClients.followRedirectHttp
      case _    => HttpClients.defaultHttp
    ) {
      url(fullUrl) OK as.String
    }.map:
      log.info(s"scraping url $fullUrl done")
      Jsoup.parse

  def get(path: String, disableSslValidation: Boolean = false): Future[Array[Byte]] =
    download(buildFullUrl(path), disableSslValidation)

  def download(fullUrl: String, disableSslValidation: Boolean = false): Future[Array[Byte]] =
    val http = disableSslValidation match
      case true  => HttpClients.insecureHttp
      case false => HttpClients.defaultHttp
    http(url(fullUrl).OK(as.Bytes))
      .map { (bytes: Array[Byte]) =>
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

object HttpClients extends Log:
  private val insecureSslContext = SslContextBuilder
    .forClient()
    .sslProvider(SslProvider.JDK)
    .trustManager(InsecureTrustManagerFactory.INSTANCE)
    .build()

  val defaultHttp        = Http.default
  val insecureHttp       = Http.withConfiguration(_.setSslContext(insecureSslContext))
  val followRedirectHttp = Http.withConfiguration(_.setFollowRedirect(true))

  def shutdown() =
    log.info(s"Shutdown...")

    defaultHttp.shutdown()
    insecureHttp.shutdown()
    followRedirectHttp.shutdown()

    log.info(s"Shutdown... terminated")
