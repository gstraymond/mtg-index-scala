package fr.gstraymond.scraper

import fr.gstraymond.model.{Price, ScrapedCard, ScrapedPrice}
import fr.gstraymond.utils.StringUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PriceScraper extends MTGGoldFishScraper {

  val path = "/prices/select"
  val sep = " - "

  def scrap: Future[Seq[ScrapedPrice]] = {
    scrapEditionUrls.flatMap { editionUrls =>
      val init: Future[Seq[ScrapedPrice]] = Future.successful(Nil)
      editionUrls.foldLeft(init) { (acc, editionUrl) =>
        for {
          prices <- acc
          _ = Thread.sleep(100)
          eventualPrices = scrapEditionPrices(editionUrl)
          eventualFoilPrices = scrapEditionPrices(editionUrl + "_F")
          newPrices <- eventualPrices
          newPricesFoil <- eventualFoilPrices.map(_.map(p => p.copy(price = 0, foilPrice = Some(p.price))))
        } yield {
          prices ++ mergePrices(newPrices, newPricesFoil)
        }
      }
    }
  }

  private def mergePrices(newPrices: Seq[ScrapedPrice],
                          newPricesFoil: Seq[ScrapedPrice]): Seq[ScrapedPrice] = {
    (newPrices ++ newPricesFoil).groupBy(_.card).values.toSeq.map {
      case Seq(p) => p
      case Seq(p1, p2) => p1.copy(
        price = p1.price + p2.price,
        foilPrice = p1.foilPrice.orElse(p2.foilPrice)
      )
    }
  }

  def process(cards: Seq[ScrapedCard], prices: Seq[ScrapedPrice]): Seq[ScrapedCard] = {
    def normEditions(price: ScrapedPrice): ScrapedPrice = {
      val code = price.editionCode
        .replace("prm-sdcc13", "prm-med")
        .replace("prm-sdcc14", "prm-med")
        .replace("prm-sdcc15", "prm-med")
      //.replace("prm-wpn", "prm-gwp")
      price.copy(editionCode = code)
    }

    merge(
      cards,
      prices.map(normEditions).flatMap(priceAsMap).toMap
    )
  }

  private def priceAsMap(card: ScrapedPrice): Seq[(String, ScrapedPrice)] = {
    def getTitle1(t1: String, t2: String) = normalize(s"$t1 ($t1/$t2)")

    def getTitle2(t1: String, t2: String) = normalize(s"$t2 ($t1/$t2)")

    def normalize(text: String): String = {
      StringUtils.normalize(text) match {
        case t if t.endsWith(")") && !t.contains("/") => t.split(" \\(").head
        case t => t
      }
    }

    card.card match {
      case title if title.contains(" // ") =>
        val title1 = title.split(" // ")(0)
        val title2 = title.split(" // ")(1)
        Seq(
          s"${card.editionCode}$sep${getTitle1(title1, title2)}" -> card,
          s"${card.editionCode}$sep${getTitle2(title1, title2)}" -> card
        )
      case _ =>
        Seq(s"${card.editionCode}$sep${normalize(card.card)}" -> card)
    }
  }

  def scrapEditionUrls: Future[Seq[String]] = {
    scrap(path).map { doc =>
      doc
        .select("div.priceList-selectMenu li[role=presentation]").asScala
        .filter(_.select("a img").asScala.nonEmpty)
        .map { li =>
          li.select("a").asScala.head.attr("href")
        }
    }
  }

  def scrapEditionPrices(path: String): Future[Seq[ScrapedPrice]] = {
    def parseDouble(str: String) = str.replace(",", "").toDouble

    scrap(path).map { doc =>
      val editionCode = path.split("/").last.toLowerCase
      val editionName = doc.select(".price-card-name-header-name").text()
      doc
        .select("div.index-price-table-paper table tr").asScala
        .filter(_.select("td").asScala.nonEmpty)
        .map { tr =>
          tr.select("td").asScala match {
            case Seq(card, _, _, price, _, _, _, _) =>
              ScrapedPrice(
                card.text(),
                editionCode,
                editionName,
                parseDouble(price.text()),
                None
              )
          }
        }
    }.recover { case e: Exception =>
      log.error(s"error parsing $host $path -- ${e.getMessage}")
      Nil
    }
  }

  private def merge(cards: Seq[ScrapedCard], cardToPrice: Map[String, ScrapedPrice]): Seq[ScrapedCard] = {

    val cardEditions = cards.map { card =>
      card.edition.name -> card.edition.code
    }.distinct

    val priceEditions = cardToPrice.values.toSeq.map { price =>
      price.editionName -> price.editionCode
    }.distinct.toMap

    val editionMapping = Map(
      "cp" -> "prm-chp",
      "fnmp" -> "prm-fnm",
      "jr" -> "prm-jud",
      "ptc" -> "prm-pre",
      "pvc" -> "dde",
      "mbp" -> "prm-med",
      "mgdc" -> "prm-gdp",
      "ugin" -> "prm-ugf",
      "mlp" -> "prm-lpc",
      "rep" -> "prm-rel",
      "sus" -> "prm-jss",
      "gpx" -> "prm-gpp",
      "9eb" -> "9ed",
      "8eb" -> "8ed",
      "hho" -> "prm-spo",
      "grc" -> "prm-wpn", // grc -> prm-gwp
      "pro" -> "prm-ptp"
    )

    val editionCodeMap = cardEditions.collect {
      case (name, code) if priceEditions.contains(name) => code -> priceEditions(name)
    }.toMap ++ editionMapping


    val mutablePrices = mutable.Map() ++ cardToPrice
    val result = cards
      .map { card =>
        val editionCode = editionCodeMap.getOrElse(card.edition.code, card.edition.code)
        val key = s"$editionCode$sep${StringUtils.normalize(card.title)}"
        mutablePrices
          .get(key)
          .fold(card) { p =>
            mutablePrices.remove(key)
            card.copy(price = Some(Price(p.price, p.foilPrice)))
          }
      }

    val editionCodes = mutablePrices.keys.map(_.split(sep).head)
    val missingPriceByEditions = mutablePrices.values.groupBy(_.editionCode)

    log.info(s"cards total before: ${cards.size} / after ${result.size}")
    log.info(s"missing prices before ${cardToPrice.size} / after ${mutablePrices.size}")
    log.info(s"missing editions before ${editionCodes.size} / ${editionCodes.mkString(", ")}")
    log.info(s"missing editions grouped ${missingPriceByEditions.mapValues(_.size).toSeq.sortBy(-_._2)}")
    //log.info(s"missing first card ${missingPriceByEditions.mapValues(_.head.card)}")
    mutablePrices.mapValues(_.card).toSeq.sortBy(_._1).foreach(t => log.info(s"missing: $t"))
    result
  }

}
