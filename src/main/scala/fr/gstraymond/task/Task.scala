package fr.gstraymond.task

import java.io.{File, FileInputStream}
import java.time.Instant
import java.time.temporal.ChronoUnit

import fr.gstraymond.model._
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.scraper.HttpClients
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.{FileUtils, Log}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

trait Task[A] extends Log {

  private val name = getClass.getSimpleName.replace("$", "")

  def main(args: Array[String]): Unit = {

    val timing = Timing(name) {
      val eventualProcess = process.recover {
        case NonFatal(e) => log.error(s"error during $name", e)
      }
      Await.result(eventualProcess, Duration.Inf)
    }

    log.info(s"Task terminated\n${Json.prettyPrint(timing.json)}")
    HttpClients.shutdown()
  }

  def process: Future[A]

  import fr.gstraymond.model.MTGCardFormat._
  import fr.gstraymond.model.MTGJsonFormats._
  import fr.gstraymond.model.MTGSetCardFormat._
  import fr.gstraymond.model.ScrapedCardFormat._
  import fr.gstraymond.model.ScrapedFormatFormat._
  import fr.gstraymond.model.ScrapedPriceFormat._
  import fr.gstraymond.rules.model.RuleFormats._

  protected def storeScrapedCards(cards: Seq[ScrapedCard]) = {
    mkDir(FileUtils.scrapPath)
    val file = new File(s"${FileUtils.scrapPath}/cards.json")
    FileUtils.storeJson(file, Json.toJson(cards.sortBy(_.uniqueId)))
    cards
  }

  protected def storeMTGCards(cards: Seq[MTGCard]) = {
    mkDir(FileUtils.outputPath)
    val file = new File(s"${FileUtils.outputPath}/cards.json")
    FileUtils.storeJson(file, Json.toJson(cards.sortBy(_.title)))
    cards
  }

  protected def storePrices(prices: Seq[ScrapedPrice]) = {
    mkDir(FileUtils.scrapPath)
    val file = new File(s"${FileUtils.scrapPath}/prices.json")
    FileUtils.storeJson(file, Json.toJson(prices))
    prices
  }

  protected def storeFormats(formats: Seq[ScrapedFormat]) = {
    mkDir(FileUtils.scrapPath)
    val file = new File(s"${FileUtils.scrapPath}/formats.json")
    FileUtils.storeJson(file, Json.toJson(formats))
    formats
  }

  protected def storeMTGSetCards(cardByEditions: Map[String, Seq[MTGSetCard]]) = {
    mkDir(FileUtils.outputPath)
    cardByEditions.foreach { case (edition, mtgSetCards) =>
      val file = new File(s"${FileUtils.outputPath}/$edition.json")
      FileUtils.storeJson(file, Json.toJson(mtgSetCards))
    }

    cardByEditions
  }

  protected def storeRules(rules: Rules) = {
    mkDir(FileUtils.outputPath)
    val file = new File(s"${FileUtils.outputPath}/rules.json")
    FileUtils.storeJson(file, Json.toJson(rules))
    rules
  }

  protected def loadAllSet: Map[String, MTGJsonEdition] = {
    val json = new FileInputStream(s"${FileUtils.scrapPath}/AllSets.json")
    Json.parse(json).as[Map[String, MTGJsonEdition]]
  }

  protected def loadScrapedCards: Seq[ScrapedCard] = {
    val json = new FileInputStream(s"${FileUtils.scrapPath}/cards.json")
    Json.parse(json).as[Seq[ScrapedCard]]
  }

  protected def pricesUpToDate: Boolean = {
    val prices = new File(s"${FileUtils.scrapPath}/prices.json")
    val lastModified = Instant.ofEpochMilli(prices.lastModified())
    log.info(s"pricesUpToDate: lastModified $lastModified")
    prices.exists() && lastModified.isAfter(Instant.now.minus(1, ChronoUnit.DAYS))
  }

  protected def loadPrices: Seq[ScrapedPrice] = {
    val json = new FileInputStream(s"${FileUtils.scrapPath}/prices.json")
    Json.parse(json).as[Seq[ScrapedPrice]]
  }

  protected def loadFormats: Seq[ScrapedFormat] = {
    val json = new FileInputStream(s"${FileUtils.scrapPath}/formats.json")
    Json.parse(json).as[Seq[ScrapedFormat]]
  }

  protected def loadMTGCards: Seq[MTGCard] = {
    val json = new FileInputStream(s"${FileUtils.outputPath}/cards.json")
    Json.parse(json).as[Seq[MTGCard]]
  }

  protected def loadRules: Seq[Rules] = {
    val json = new FileInputStream(s"${FileUtils.outputPath}/rules.json")
    Seq(Json.parse(json).as[Rules])
  }

  private def mkDir(path: String) = {
    val dir = new File(path)
    if (!dir.exists()) dir.mkdirs()
  }
}