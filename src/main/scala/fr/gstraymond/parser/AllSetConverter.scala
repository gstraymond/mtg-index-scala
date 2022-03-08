package fr.gstraymond.parser

import fr.gstraymond.constant.URIs
import fr.gstraymond.model._
import fr.gstraymond.parser.PriceModels._
import fr.gstraymond.parser.field._
import fr.gstraymond.utils.Log
import fr.gstraymond.utils.StringUtils

import java.text.SimpleDateFormat
import java.time.LocalDate
import scala.concurrent.Future

object AllSetConverter
    extends Log
    with AbilitiesField
    with ColorField
    with DevotionsField
    with FormatsField
    with HiddenHintsField
    with LandField
    with PriceRangesField
    with SpecialField:

  private val dateParser = new SimpleDateFormat("yyyy-MM-dd")

  private val editionsCodeWithoutImage = Set(
    "ANA",
    "CP1",
    "CP2",
    "CP3",
    "F01",
    "F02",
    "F03",
    "F04",
    "F05",
    "F06",
    "F07",
    "F08",
    "F09",
    "F10",
    "F11",
    "F12",
    "F13",
    "F14",
    "F15",
    "F16",
    "F17",
    "F18",
    "FBB",
    "FNM",
    "G00",
    "G01",
    "G02",
    "G03",
    "G04",
    "G05",
    "G06",
    "G07",
    "G08",
    "G09",
    "G10",
    "G11",
    "G99",
    "H17",
    "HHO",
    "HTR",
    "J12",
    "J13",
    "J14",
    "J15",
    "J16",
    "J17",
    "J18",
    "JGP",
    "L12",
    "L13",
    "L14",
    "L15",
    "L16",
    "L17",
    "MED",
    "MP2",
    "MPR",
    "P03",
    "P04",
    "P05",
    "P06",
    "P07",
    "P08",
    "P09",
    "P10",
    "P11",
    "PR2",
    "PRM",
    "PTC",
    "PZ1",
    "PZ2",
    "REN",
    "RIN",
    "SUM",
    "TD0",
    "CEI",
    "CED",
    "ATH",
    "ITP",
    "DKM",
    "RQS",
    "DPA",
    "CST",
    "MGB",
    "CPK"
  )

  def convert(
      loadAllSet: Map[String, MTGJsonEdition],
      abilities: Seq[String],
      prices: Seq[CardPrice]
  ): Future[Seq[MTGCard]] = Future.successful {
    val groupedPrices = prices.groupBy(_.uuid).view.mapValues(_.head).toMap

    val nextWeek = LocalDate.now().plusWeeks(1)

    val allCards = loadAllSet.values
      .filter(_.releaseDate.exists(LocalDate.parse(_).isBefore(nextWeek)))
      .filterNot(_.isOnlineOnly.getOrElse(false))
      .flatMap(edition => edition.cards.map(_ -> edition.copy(cards = Nil)))
      .groupBy { case (card, _) => (card.faceName.getOrElse(card.name), card.manaCost, card.`type`) }
      .values

    val result = allCards.map { groupedCards =>
      val groupedCardsSorted = groupedCards.toSeq.sortBy { case (_, edition) =>
        dateParser.parse(edition.releaseDate.getOrElse("1970-01-01"))
      }
      val cards      = groupedCardsSorted.map(_._1)
      val editions   = groupedCardsSorted.map(_._2)
      val firstCard  = cards.head
      val cardPrices = cards.map(_.uuid).flatMap(groupedPrices.get(_))
      val castingCost = firstCard.manaCost.map {
        _.replace("}{", " ")
          .replace("{", "")
          .replace("}", "")
          .replace("2/", "2#")
          .replace("/", "")
          .replace("2#", "2/")
      }
      val description = firstCard.text.map(_.split("\n").toSeq).getOrElse(Seq.empty)
      val hints       = _hiddenHints(description)
      val title       = firstCard.faceName.getOrElse(firstCard.name)
      val urlTitle    = StringUtils.normalize(title)
      val rarities    = cards.map(_.rarity.replace("timeshifted ", "")).distinct

      MTGCard(
        title = title,
        altTitles = firstCard.name.split(" // ").filterNot(_ == title).toSeq,
        frenchTitle = cards.flatMap(_.foreignData).flatten.find(_.language == "French").flatMap(_.name),
        castingCost = castingCost,
        colors = _colors(castingCost, hints, firstCard.colors),
        dualColors = _dualColors(castingCost, firstCard.colors),
        tripleColors = _tripleColors(castingCost, firstCard.colors),
        quadColors = _quadColors(castingCost, firstCard.colors),
        convertedManaCost = firstCard.convertedManaCost.map(_.toInt).getOrElse(0),
        `type` = firstCard.`type`,
        description = firstCard.text.getOrElse(""),
        power = firstCard.power,
        toughness = firstCard.toughness,
        editions = editions.map(_.name).distinct,
        rarities = rarities,
        priceRanges = _priceRanges(cardPrices),
        minPaperPriceRange = _minPaperPriceRange(cardPrices),
        minMtgoPriceRange = _minMtgoPriceRange(cardPrices),
        publications = groupedCardsSorted.map { case (card, edition) =>
          val rarity      = card.rarity.replace("timeshifted ", "")
          val rarityCode  = rarity.head.toString.toUpperCase
          val editionCode = edition.code.toUpperCase
          val stdEditionCode = Some(gathererMap.getOrElse(editionCode, editionCode)).filter { code =>
            code.length < 4 && !editionsCodeWithoutImage(code)
          }
          Publication(
            collectorNumber = card.number,
            edition = edition.name match {
              case name if name.contains(",") => name.replace(",", ":")
              case name                       => name
            },
            editionCode = editionCode,
            editionReleaseDate = Some(dateParser.parse(edition.releaseDate.getOrElse("1970-01-01")).getTime),
            stdEditionCode = stdEditionCode,
            rarity = rarity,
            rarityCode = Some(rarityCode),
            image = card.identifiers.multiverseId.map { multiverseId =>
              s"${URIs.pictureHost}/pics/$editionCode/$multiverseId-$urlTitle.jpg"
            },
            editionImage = stdEditionCode.map { code =>
              s"${URIs.pictureHost}/sets/$code/$rarityCode.gif"
            },
            price = computePrices(groupedPrices.get(card.uuid)),
            foilPrice = computePrices(groupedPrices.get(card.uuid), foil = true),
            mtgoPrice = computePrices(groupedPrices.get(card.uuid), online = true),
            mtgoFoilPrice = computePrices(groupedPrices.get(card.uuid), foil = true, online = true),
            block = edition.block,
            multiverseId = card.identifiers.multiverseId.flatMap(_.toLongOption)
          )
        },
        abilities = _abilities(title, description, abilities),
        formats = _formats(cards.head.legalities.map(processLegalities).getOrElse(Seq.empty), editions),
        artists = cards.flatMap(_.artist).distinct,
        devotions = _devotions(Some(firstCard.`type`), castingCost),
        blocks = editions.flatMap(_.block).distinct,
        layout = firstCard.layout,
        loyalty = firstCard.loyalty,
        special = _special(title, firstCard.`type`, description),
        land = _land(firstCard.`type`, description),
        ruling = firstCard.rulings.getOrElse(Nil).map(r => Ruling(r.date, r.text))
      )
    }.toSeq

    log.info(s"cards total before: ${allCards.size} / after ${result.size}")

    result
  }

  implicit val ord: Ordering[LocalDate] = _ compareTo _

  private def computePrices(price: Option[CardPrice], foil: Boolean = false, online: Boolean = false) =
    val cardPrice = if online then price.flatMap(_.online) else price.flatMap(_.paper)
    if foil then cardPrice.flatMap(_.foil)
    else cardPrice.flatMap(_.normal)

  private def processLegalities(data: Map[String, String]): Seq[MTGJsonLegality] =
    data.toSeq
      .map { case (format, legality) => MTGJsonLegality(format, legality) }

  val gathererMap: Map[String, String] = Map(
    "2ED" -> "2U",
    "3ED" -> "3E",
    "4ED" -> "4E",
    "5ED" -> "5E",
    "6ED" -> "6E",
    "7ED" -> "7E",
    "ALL" -> "AL",
    "APC" -> "AP",
    "ARN" -> "AN",
    "ATQ" -> "AQ",
    "BRB" -> "BR",
    "BTD" -> "BD",
    "CHR" -> "CH",
    "DRK" -> "DK",
    "EXO" -> "EX",
    "FEM" -> "FE",
    "HML" -> "HM",
    "ICE" -> "IA",
    "INV" -> "IN",
    "LEA" -> "1E",
    "LEB" -> "2E",
    "LEG" -> "LE",
    "MIR" -> "MI",
    "MMQ" -> "MM",
    "MPS" -> "MPS_KLD",
    "NEM" -> "NE",
    "ODY" -> "OD",
    "PCY" -> "PR",
    "PLS" -> "PS",
    "P02" -> "P2",
    "POR" -> "PO",
    "PTK" -> "PK",
    "S00" -> "P4",
    "S99" -> "P3",
    "STH" -> "ST",
    "TMP" -> "TE",
    "UDS" -> "CG",
    "UGL" -> "UG",
    "ULG" -> "GU",
    "USG" -> "UZ",
    "VIS" -> "VI",
    "WTH" -> "WL",
    "DD1" -> "EVG",
    "DVD" -> "DD3_DVD",
    "GVL" -> "DD3_GVL",
    "JVC" -> "DD3_JVC",
    "ME1" -> "MED"
  )
