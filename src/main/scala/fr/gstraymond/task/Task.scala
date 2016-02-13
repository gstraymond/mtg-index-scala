package fr.gstraymond.task

import java.io.File

import fr.gstraymond.model._
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.{FileUtils, Log}
import play.api.libs.json.Json

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.{Success, Failure}

/**
  * Created by guillaume on 10/02/16.
  */
trait Task[A] extends Log {

  val name = getClass.getSimpleName.replace("$", "")

  def main(args: Array[String]): Unit = {

    val timing = Timing(name)(
      Await.ready(process, Duration.Inf).value.get match {
        case Failure(e) => log.error("error during scrap", e)
        case Success(s) => s
      }
    )

    log.info(Json.prettyPrint(timing.json))
  }

  def process: Future[A]

  import fr.gstraymond.model.RawCardFormat._
  import fr.gstraymond.model.ScrapedCardFormat._
  import fr.gstraymond.model.ScrapedEditionFormat._
  import fr.gstraymond.model.ScrapedPriceFormat._
  import fr.gstraymond.model.ScrapedFormatFormat._

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

  protected def loadRawCards: Seq[RawCard] = {
    val json = Source.fromFile(s"${FileUtils.oraclePath}/cards.json").mkString
    Json.parse(json).as[Seq[RawCard]]
  }

  protected def loadScrapedCards: Seq[ScrapedCard] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/cards.json").mkString
    Json.parse(json).as[Seq[ScrapedCard]]
  }

  protected def loadPrices: Seq[ScrapedPrice] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/prices.json").mkString
    Json.parse(json).as[Seq[ScrapedPrice]]
  }

  private def mkDir(path: String) = {
    val dir = new File(path)
    if (!dir.exists()) dir.mkdirs()
  }
}
