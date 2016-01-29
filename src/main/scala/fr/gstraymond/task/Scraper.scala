package fr.gstraymond.task

import java.io.File

import fr.gstraymond.model.ScrapedCardFormat._
import fr.gstraymond.model.ScrapedEditionFormat._
import fr.gstraymond.scraper.{CardScraper, EditionScraper}
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.Log
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Failure

object Scraper extends Log {

  val langs = Seq("en", "fr")
  val scrapPath = "src/main/resources/scrap"


  def main(args: Array[String]): Unit = {

    val editionFile = new File(s"$scrapPath/editions.json")
    val cardFile = new File(s"$scrapPath/cards.json")

    val timing = Timing("scrap") {
      for {
        editions <- EditionScraper.scrap
        cards <- CardScraper.scrap(editions, langs)
      } yield {
        printJson(editionFile, Json.toJson(editions.sortBy(_.code)))
        printJson(cardFile, Json.toJson(cards.sortBy(_.uniqueId)))
      }
    }.map { eventualResult =>
      Await.ready(eventualResult, Duration.Inf).value.get match {
        case Failure(e) => log.error("error during scrap", e)
        case _ =>
      }
    }

    log.info(Json.prettyPrint(timing.json))
  }

  private def printJson(file: java.io.File, json: JsValue) {
    val writer = new java.io.PrintWriter(file)
    try {
      writer.println(Json.prettyPrint(json))
    } finally {
      writer.close()
    }
  }
}