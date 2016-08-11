package fr.gstraymond.parser

import java.text.SimpleDateFormat

import fr.gstraymond.constant.URIs
import fr.gstraymond.model._
import fr.gstraymond.utils.{Log, StringUtils}

import scala.collection.mutable
import scala.concurrent.Future

object AllSetConverter extends Log {

  private val dateParser = new SimpleDateFormat("YYYY-MM-DD")

  val editionsCodeWithoutImage = Seq("CEI", "CED", "ATH", "ITP", "DKM", "RQS", "DPA", "CST", "MGB", "CPK")

  def convert(
    loadAllSet: Map[String, MTGJsonEdition],
    formats: Seq[ScrapedFormat],
    prices: Seq[ScrapedPrice]
  ): Future[Seq[MTGCard]] = Future.successful {

    val priceMap = mutable.Map() ++ prices.flatMap {
      case price if price.card.contains(" // ") =>
        price.card.split(" // ").map { card => price.copy(card = card) }
      case price if price.card.contains(" (") && price.card.endsWith(")") =>
        Seq(price.copy(card = price.card.split(" \\(").head))
      case price =>
        Seq(price)
    }.groupBy { price =>
      StringUtils.normalize(price.card) -> price.editionCode
    }.mapValues(_.head)

    val allCards = loadAllSet.values.flatMap { edition =>
      val editionWithNoCards = edition.copy(cards = Seq.empty)
      edition.cards.map { card =>
        card -> editionWithNoCards
      }
    }.groupBy { case (card, edition) =>
      (card.name, card.manaCost, card.`type`)
    }.values

    val result = allCards.map { groupedCards =>
      val groupedCardsSorted = groupedCards.toSeq.sortBy { case (_, edition) =>
        dateParser.parse(edition.releaseDate)
      }.map { case (card, edition) =>
        val urlTitle = StringUtils.normalize(card.name)
        val codes = Seq(Some(edition.code), edition.magicCardsInfoCode, edition.gathererCode).flatten.distinct
        (card, edition, _price(urlTitle, codes, priceMap))
      }
      val cards = groupedCardsSorted.map(_._1)
      val editions = groupedCardsSorted.map(_._2)
      val prices = groupedCardsSorted.flatMap(_._3)
      val firstCard = cards.head
      val castingCost = firstCard.manaCost.map {
        _.replace("}{", " ")
          .replace("{", "")
          .replace("}", "")
          .replace("2/", "2#")
          .replace("/", "")
          .replace("2#", "2/")
      }
      val description = firstCard.text.map(_.split("\n").toSeq).getOrElse(Seq.empty)
      val hints = CardConverter._hiddenHints(description)
      val urlTitle = StringUtils.normalize(firstCard.name)
      MTGCard(
        title = firstCard.name,
        altTitles = firstCard.names.getOrElse(Seq.empty),
        frenchTitle = cards.flatMap(_.foreignNames).flatten.find(_.language == "French").map(_.name),
        castingCost = castingCost,
        colors = CardConverter._colors(castingCost, hints, firstCard.colors),
        convertedManaCost = firstCard.cmc.map(_.toInt).getOrElse(0),
        `type` = firstCard.`type`,
        description = firstCard.text.getOrElse(""),
        power = firstCard.power,
        toughness = firstCard.toughness,
        editions = editions.map(_.name).distinct,
        rarities = cards.map(_.rarity).distinct,
        priceRanges = CardConverter._priceRanges(prices),
        publications = groupedCardsSorted.map { case (card, edition, price) =>
          val rarity = card.rarity.replace("Basic ", "")
          val rarityCode = rarity.head.toString
          val stdEditionCode = edition.gathererCode.orElse(Some(edition.code).filter { code =>
            !(code.length == 4 && code.startsWith("p")) &&
              !editionsCodeWithoutImage.contains(code)
          })
          Publication(
            collectorNumber = card.number,
            edition = edition.name match {
              case name if name.contains(",") => name.replace(",", ":")
              case name => name
            },
            editionCode = edition.code,
            editionReleaseDate = Some(dateParser.parse(edition.releaseDate)),
            stdEditionCode = stdEditionCode,
            rarity = rarity,
            rarityCode = Some(rarityCode),
            image = card.multiverseid.map { multiverseId =>
              s"${URIs.pictureHost}/pics/${edition.code}/$multiverseId-$urlTitle.jpg"
            },
            editionImage = stdEditionCode.map { code =>
              s"${URIs.pictureHost}/sets/$code/$rarityCode.gif"
            },
            price = price,
            block = edition.block,
            multiverseId = card.multiverseid
          )
        },
        abilities = CardConverter._abilities(Some(firstCard.`type`), description),
        formats = CardConverter._formats(formats, Some(firstCard.`type`), description, firstCard.name, editions.map(_.name)),
        artists = cards.map(_.artist).distinct,
        devotions = CardConverter._devotions(Some(firstCard.`type`), castingCost),
        blocks = editions.flatMap(_.block).distinct,
        layout = firstCard.layout
      )
    }.toSeq

    log.info(s"cards total before: ${allCards.size} / after ${result.size}")
    log.info(s"missing prices before ${prices.size} / after ${priceMap.size}")
    priceMap.keys.toSeq.map(_._2).groupBy(a => a).mapValues(_.size).toSeq.sortBy(-_._2).foreach(t => log.info(s"missing edition: $t"))
    priceMap.mapValues(_.card).toSeq.sortBy(_._1).foreach(t => log.info(s"missing card: $t"))

    result
  }

  def _price(name: String, codes: Seq[String], priceMap: mutable.Map[(String, String), ScrapedPrice]) = {

    def getPrice(name: String, code: String): Option[Double] = {
      val k = name -> priceCodeMap.getOrElse(code, code.toLowerCase)
      priceMap.get(k).map {
        priceMap remove k
        _.price
      }
    }

    codes.foldLeft(None: Option[Double]) {
      (acc, code) =>
        acc.orElse {
          getPrice(name, code)
        }
    }
  }

  val priceCodeMap = Map(
    "pPRE" -> "prm-pre",
    "pFNM" -> "prm-fnm",
    "pJGP" -> "prm-jud",
    "pSUS" -> "prm-jss",
    "pMEI" -> "prm-med",
    "pMGD" -> "prm-gdp",
    "pMPR" -> "prm-mpr",
    "pGTW" -> "prm-gwp",
    "FRF_UGIN" -> "prm-ugf",
    "pWPN" -> "prm-wpn",
    "pLPA" -> "prm-lpc",
    "pREL" -> "prm-rel",
    "pCMP" -> "prm-chp",
    "pGPX" -> "prm-gpp",
    "pPRO" -> "prm-ptp",
    "HOP" -> "pc1"
    // prm-msc ?
    // prm-spo ?
  )
}
