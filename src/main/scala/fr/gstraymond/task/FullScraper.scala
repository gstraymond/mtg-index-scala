package fr.gstraymond.task

import java.io.File

import fr.gstraymond.model.ScrapedCardFormat._
import fr.gstraymond.model.ScrapedEditionFormat._
import fr.gstraymond.model.ScrapedPriceFormat._
import fr.gstraymond.model.ScrapedFormatFormat._
import fr.gstraymond.model.{ScrapedFormat, ScrapedEdition, ScrapedCard, ScrapedPrice}
import fr.gstraymond.scraper._
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.{FileUtils, Log}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

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

  protected def storeCards(cards: Seq[ScrapedCard]) = {
    val file = new File(s"${FileUtils.scrapPath}/cards.json")
    FileUtils.printJson(file, Json.toJson(cards.sortBy(_.uniqueId)))
    cards
  }

  protected def storeEditions(editions: Seq[ScrapedEdition]) = {
    val file = new File(s"${FileUtils.scrapPath}/editions.json")
    FileUtils.printJson(file, Json.toJson(editions.sortBy(_.code)))
    editions
  }

  protected def storePrices(prices: Seq[ScrapedPrice]) = {
    val file = new File(s"${FileUtils.scrapPath}/prices.json")
    FileUtils.printJson(file, Json.toJson(prices))
    prices
  }

  protected def storeFormats(formats: Seq[ScrapedFormat]) = {
    val file = new File(s"${FileUtils.scrapPath}/formats.json")
    FileUtils.printJson(file, Json.toJson(formats))
    formats
  }
}

object FullScrapTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    for {
      editions <- EditionScraper.scrap
      editionsWithDate <- ReleaseDateScraper.scrap(editions)
      cards <- CardScraper.scrap(editionsWithDate, FileUtils.langs)
      cardsWithPrice <- PriceScraper.scrapAndProcess(cards)
    } yield {
      storeEditions(editionsWithDate)
      storeCards(cardsWithPrice)
    }
  }
}

object CardScrapTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    for {
      editions <- EditionScraper.scrap
      editionsWithDate <- ReleaseDateScraper.scrap(editions)
      cards <- CardScraper.scrap(editionsWithDate, FileUtils.langs)
    } yield {
      storeEditions(editionsWithDate)
      storeCards(cards)
    }
  }
}

object PriceScrapTask extends Task[Seq[ScrapedPrice]] {
  override def process = {
    PriceScraper.scrap.map(storePrices)
  }
}

object PriceProcessTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    Future.successful {
      PriceScraper.process
    }
  }
}
object FormatScrapTask extends Task[Seq[ScrapedFormat]] {
  override def process = FormatScraper.scrap.map(storeFormats)
}

