package fr.gstraymond.task

import java.io.File

import fr.gstraymond.model.ScrapedCardFormat._
import fr.gstraymond.model.ScrapedEditionFormat._
import fr.gstraymond.model.ScrapedPriceFormat._
import fr.gstraymond.scraper.{CardScraper, EditionScraper, PriceScraper, ReleaseDateScraper}
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.{FileUtils, Log}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Failure

object Scraper extends Log {

  def main(args: Array[String]): Unit = {
    val editionFile = new File(s"${FileUtils.scrapPath}/editions.json")
    val cardFile = new File(s"${FileUtils.scrapPath}/cards.json")

    val timing = Timing("scrap") {
      for {
        editions <- EditionScraper.scrap
        editionsWithDate <- ReleaseDateScraper.scrap(editions)
        cards <- CardScraper.scrap(editionsWithDate, FileUtils.langs)
        cardsWithPrice <- Future.successful(PriceScraper.process(cards))
      } yield {
        FileUtils.printJson(editionFile, Json.toJson(editionsWithDate.sortBy(_.code)))
        FileUtils.printJson(cardFile, Json.toJson(cardsWithPrice.sortBy(_.uniqueId)))
      }
    }.map { eventualResult =>
      Await.ready(eventualResult, Duration.Inf).value.get match {
        case Failure(e) => log.error("error during scrap", e)
        case _ =>
      }
    }

    log.info(Json.prettyPrint(timing.json))
  }
}

object MCIScraper extends Log {

  def main(args: Array[String]): Unit = {
    val editionFile = new File(s"${FileUtils.scrapPath}/editions.json")
    val cardFile = new File(s"${FileUtils.scrapPath}/cards.json")

    val timing = Timing("scrap") {
      for {
        editions <- EditionScraper.scrap
        editionsWithDate <- ReleaseDateScraper.scrap(editions)
        cards <- CardScraper.scrap(editionsWithDate, FileUtils.langs)
      } yield {
        FileUtils.printJson(editionFile, Json.toJson(editionsWithDate.sortBy(_.code)))
        FileUtils.printJson(cardFile, Json.toJson(cards.sortBy(_.uniqueId)))
      }
    }.map { eventualResult =>
      Await.ready(eventualResult, Duration.Inf).value.get match {
        case Failure(e) => log.error("error during scrap", e)
        case _ =>
      }
    }

    log.info(Json.prettyPrint(timing.json))
  }
}

object MGFScraper extends Log {

  def main(args: Array[String]): Unit = {
    val priceFile = new File(s"${FileUtils.scrapPath}/prices.json")

    val timing = Timing("prices") {
      PriceScraper.scrap.map { prices =>
        FileUtils.printJson(priceFile, Json.toJson(prices))
      }
    }.map { eventualResult =>
      Await.ready(eventualResult, Duration.Inf).value.get match {
        case Failure(e) => log.error("error during scrap", e)
        case _ =>
      }
    }

    log.info(Json.prettyPrint(timing.json))
  }
}

object Process extends Log {

  def main(args: Array[String]): Unit = {
    val timing = Timing("process") {
      PriceScraper.process
    }

    log.info(Json.prettyPrint(timing.json))
  }
}

