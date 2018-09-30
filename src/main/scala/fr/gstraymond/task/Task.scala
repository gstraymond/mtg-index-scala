package fr.gstraymond.task

import java.io.{File, FileNotFoundException}

import fr.gstraymond.model._
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.scraper.HttpClients
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.{FileUtils, Log}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.{Codec, Source}
import scala.util.control.NonFatal

/**
  * Created by guillaume on 10/02/16.
  */
trait Task[A] extends Log {

  val name = getClass.getSimpleName.replace("$", "")

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
  import fr.gstraymond.model.RawCardFormat._
  import fr.gstraymond.model.ScrapedCardFormat._
  import fr.gstraymond.model.ScrapedEditionFormat._
  import fr.gstraymond.model.ScrapedFormatFormat._
  import fr.gstraymond.model.ScrapedPriceFormat._
  import fr.gstraymond.rules.model.RuleFormats._

  protected def storeRawCards(cards: Seq[RawCard]) = {
    mkDir(FileUtils.oraclePath)
    val file = new File(s"${FileUtils.oraclePath}/cards.json")
    FileUtils.storeJson(file, Json.toJson(cards.sortBy(_.title)))
    cards
  }

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

  protected def storeEditions(editions: Seq[ScrapedEdition]) = {
    mkDir(FileUtils.scrapPath)
    val file = new File(s"${FileUtils.scrapPath}/editions.json")
    FileUtils.storeJson(file, Json.toJson(editions.sortBy(_.code)))
    editions
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

  protected def storeStdCodeCache(cache: Map[String, String]) = {
    mkDir(FileUtils.cachePath)
    val file = new File(s"${FileUtils.cachePath}/stdCodes.json")
    FileUtils.storeJson(file, Json.toJson(cache))
    cache
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

  protected def loadRawCards: Seq[RawCard] = {
    val json = Source.fromFile(s"${FileUtils.oraclePath}/cards.json").mkString
    Json.parse(json).as[Seq[RawCard]]
  }

  protected def loadAllSet: Map[String, MTGJsonEdition] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/AllSets-x.json")(Codec.UTF8).mkString
    Json.parse(json).as[Map[String, MTGJsonEdition]]
  }

  protected def loadScrapedCards: Seq[ScrapedCard] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/cards.json").mkString
    Json.parse(json).as[Seq[ScrapedCard]]
  }

  protected def loadPrices: Seq[ScrapedPrice] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/prices.json").mkString
    Json.parse(json).as[Seq[ScrapedPrice]]
  }

  protected def loadFormats: Seq[ScrapedFormat] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/formats.json").mkString
    Json.parse(json).as[Seq[ScrapedFormat]]
  }

  protected def loadEditions: Seq[ScrapedEdition] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/editions.json").mkString
    Json.parse(json).as[Seq[ScrapedEdition]]
  }

  protected def loadMTGCards: Seq[MTGCard] = {
    val json = Source.fromFile(s"${FileUtils.outputPath}/cards.json").mkString
    Json.parse(json).as[Seq[MTGCard]]
  }

  protected def loadOracle: File = {
    val files = new File(FileUtils.oraclePath).listFiles().filter(_.getName.endsWith(".txt"))
    log.info(s"files: ${files.map(_.getName).mkString(", ")}")
    val head: File = files.sortBy(_.getName).reverse.head
    log.info(s"file found: ${head.getName}")
    head
  }

  protected def loadStdCodeCache: Map[String, String] = {
    try {
      val json = Source.fromFile(s"${FileUtils.cachePath}/stdCodes.json").mkString
      Json.parse(json).as[Map[String, String]]
    } catch {
      case e: FileNotFoundException => Map.empty
    }
  }

  protected def loadRules: Seq[Rules] = {
    val json = Source.fromFile(s"${FileUtils.outputPath}/rules.json").mkString
    Seq(Json.parse(json).as[Rules])
  }

  private def mkDir(path: String) = {
    val dir = new File(path)
    if (!dir.exists()) dir.mkdirs()
  }
}