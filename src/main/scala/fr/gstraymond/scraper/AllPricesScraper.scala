package fr.gstraymond.scraper

import fr.gstraymond.parser.PriceModels.*
import fr.gstraymond.utils.FileUtils
import fr.gstraymond.utils.Log

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

import sys.process.*

object AllPricesScraper extends MtgJsonScraper, Log {

  val path = "/api/v5/AllPricesToday.json"

  def scrap: Future[Map[String, CardPricePartial]] = Future {
    new File(FileUtils.scrapPath).mkdirs()

    val command = s"curl '${buildFullUrl(path)}'" #> new File(s"${FileUtils.scrapPath}/AllPrices.orig.json")

    log.info(s"""command: $command""")
    val _ = command.!

    val command2 = s"cat ${FileUtils.scrapPath}/AllPrices.orig.json" #| "jq -c --stream ." #> new File(
      s"${FileUtils.scrapPath}/AllPrices.stream.json"
    )

    log.info(s"""command2: $command2""")
    val _ = command2.!

    var currentCardPrice: Option[CardPrice] = None
    val cardPrices                          = scala.collection.mutable.ListBuffer.empty[CardPrice]

    Source
      .fromFile(new File(s"${FileUtils.scrapPath}/AllPrices.stream.json"))
      .getLines()
      .filter(_.contains("retail"))
      .filterNot(_.contains("cardmarket"))
      .filterNot(_.contains("manapool"))
      .zipWithIndex
      .foreach { case (line, i) =>
        if i % 100000 == 0 then log.debug(s"i: $i - ${cardPrices.lastOption}")

        val elements = line
          .split('[')
          .toVector
          .flatMap(_.split(']'))
          .flatMap(_.split(','))
          .filterNot(_.isEmpty)
          .map(_.replace("\"", ""))
        elements.lastOption.flatMap(_.toDoubleOption).foreach { priceAsDouble =>
          val uuid     = elements(1)
          val isPaper  = elements(2) == "paper"
          val isNormal = elements(5) == "normal"
          val cp = {
            val price =
              if isNormal then Price(Some(priceAsDouble), None)
              else Price(None, Some(priceAsDouble))

            if isPaper then CardPrice(uuid, Some(price), None)
            else CardPrice(uuid, None, Some(price))
          }

          if Some(uuid) != currentCardPrice.map(_.uuid) then {
            currentCardPrice.foreach(cardPrices.addOne)
            currentCardPrice = Some(cp)
          } else currentCardPrice = Some(mergeCP(currentCardPrice.get, cp))
        }
      }

    currentCardPrice.foreach(cardPrices.addOne)

    cardPrices.toSeq.groupBy(_.uuid).view.mapValues(_.head).mapValues(cp => CardPricePartial(cp.paper, cp.online)).toMap
  }

  private def mergeCP(cp1: CardPrice, cp2: CardPrice): CardPrice = {
    val paper  = mergeP(cp1.paper, cp2.paper)
    val online = mergeP(cp1.online, cp2.online)
    CardPrice(cp1.uuid, paper, online)
  }

  private def mergeP(p1: Option[Price], p2: Option[Price]): Option[Price] = {
    val normal = mergeD(p1.flatMap(_.normal), p2.flatMap(_.normal))
    val foil   = mergeD(p1.flatMap(_.foil), p2.flatMap(_.foil))

    if normal.isEmpty && foil.isEmpty then None
    else Some(Price(normal, foil))
  }

  private def mergeD(p1: Option[Double], p2: Option[Double]): Option[Double] =
    p2.orElse(p1)
}
