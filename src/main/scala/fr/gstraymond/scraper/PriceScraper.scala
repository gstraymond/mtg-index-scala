package fr.gstraymond.scraper

import fr.gstraymond.model.{Price, ScrapedCard, ScrapedPrice}
import fr.gstraymond.utils.{StringUtils, FileUtils}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import fr.gstraymond.model.ScrapedCardFormat._
import fr.gstraymond.model.ScrapedPriceFormat._

object PriceScraper extends MTGGoldFishScraper {

  val path = "/prices/select"
  val sep = " - "

  def process: Seq[ScrapedCard] = {
    process(loadCards)
  }

  private def normEditions(price: ScrapedPrice): ScrapedPrice = {
    val code = price.editionCode
      .replace("prm-sdcc13", "prm-med")
      .replace("prm-sdcc14", "prm-med")
      .replace("prm-sdcc15", "prm-med")
      //.replace("prm-wpn", "prm-gwp")
    price.copy(editionCode = code)
  }

  def process(cards: Seq[ScrapedCard]): Seq[ScrapedCard] = {
    val priceMap = loadPrice.map(normEditions).flatMap(priceAsMap).toMap
    merge(cards, priceMap)
  }

  private def priceAsMap(card: ScrapedPrice): Seq[(String, ScrapedPrice)] = {
    def getTitle1(t1: String, t2: String) = normalize(s"$t1 ($t1/$t2)")
    def getTitle2(t1: String, t2: String) = normalize(s"$t2 ($t1/$t2)")
    def normalize(text: String): String = {
      text
        .replace("Aether", "ther")
        .replace("Aerathi", "rathi")
        .toLowerCase match {
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

  private def loadPrice: Seq[ScrapedPrice] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/prices.json").mkString
    Json.parse(json).as[Seq[ScrapedPrice]]
  }

  private def loadCards: Seq[ScrapedCard] = {
    val json = Source.fromFile(s"${FileUtils.scrapPath}/cards.json").mkString
    Json.parse(json).as[Seq[ScrapedCard]]
  }

  def scrap: Future[Seq[ScrapedPrice]] = {
    scrapEditionUrls.flatMap { editionUrls =>
      Future.sequence {
        editionUrls.map { editionPath =>
          scrapEditionPrices(editionPath)
        }
      }.map(_.flatten)
    }
  }

  private def scrapEditionUrls: Future[Seq[String]] = {
    get(path).map { doc =>
      doc
        .select("div.priceList-selectMenu li[role=presentation]").asScala
        .filter(_.select("a img").asScala.nonEmpty)
        .map { li =>
          li.select("a").asScala.head.attr("href")
        }
    }
  }

  private def scrapEditionPrices(path: String): Future[Seq[ScrapedPrice]] = {
    def parseDouble(str: String) = str.replace(",", "").toDouble

    get(path).map { doc =>
      val editionCode = path.split("/").last.toLowerCase
      val editionName = doc.select(".price-card-name-header-name").text()
      doc
        .select("div.index-price-table-paper table tr").asScala
        .filter(_.select("td").asScala.nonEmpty)
        .map { tr =>
          tr.select("td").asScala match {
            case Seq(card, _, _, price, daily, _, weekly, _) =>
              ScrapedPrice(
                card.text(),
                editionCode,
                editionName,
                parseDouble(price.text()),
                parseDouble(daily.text()),
                parseDouble(weekly.text())
              )
          }
        }
    }.recover { case e: Exception =>
      log.error(s"error parsing $host $path", e)
      Seq.empty
    }
  }

  private def merge(cards: Seq[ScrapedCard], cardToPrice: Map[String, ScrapedPrice]): Seq[ScrapedCard] = {

    val cardEditions = cards.map { card =>
      card.editionName -> card.editionCode
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
      case (name, code) if priceEditions.get(name).isDefined => code -> priceEditions.get(name).get
    }.toMap ++ editionMapping


    val mutablePrices = mutable.Map() ++ cardToPrice.toMap
    val r = cards
      .map { card =>
        val editionCode = editionCodeMap.getOrElse(card.editionCode, card.editionCode)
        val key = s"$editionCode$sep${StringUtils.normalize(card.title)}"
        mutablePrices
          .get(key)
          .map { p =>
            mutablePrices.remove(key)
            val price = Price(p.price, p.daily, p.weekly)
            card.copy(price = Some(price))
          }.getOrElse {
          card
        }
      }

    val editionCodes = mutablePrices.keys.map(_.split(sep).head)
    val missingPriceByEditions = mutablePrices.values.groupBy(_.editionCode)

    log.info(s"cards total before: ${cards.size} / after ${r.size}")
    log.info(s"missing prices before ${cardToPrice.size} / after ${mutablePrices.size}")
    log.info(s"missing editions before ${editionCodes.size} / ${editionCodes.mkString(", ")}")
    log.info(s"missing editions grouped ${missingPriceByEditions.mapValues(_.size).toSeq.sortBy(-_._2)}")
    //log.info(s"missing first card ${missingPriceByEditions.mapValues(_.head.card)}")
    mutablePrices.mapValues(_.card).toSeq.sortBy(_._1).foreach(t => log.info(s"missing: $t"))
    r
  }

}
