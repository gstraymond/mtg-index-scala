package fr.gstraymond.parser

import java.text.SimpleDateFormat
import java.time.LocalDate

import fr.gstraymond.constant.URIs
import fr.gstraymond.model._
import fr.gstraymond.parser.field._
import fr.gstraymond.utils.{Log, StringUtils}

import scala.collection.mutable
import scala.concurrent.Future

object AllSetConverter extends Log
  with AbilitiesField
  with ColorField
  with DevotionsField
  with FormatsField
  with HiddenHintsField
  with LandField
  with PriceRangesField
  with SpecialField {

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

  def convert(loadAllSet: Map[String, MTGJsonEdition],
              formats: Seq[ScrapedFormat],
              prices: Seq[ScrapedPrice],
              abilities: Seq[String]): Future[Seq[MTGCard]] = Future.successful {

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

    val nextWeek = LocalDate.now().plusWeeks(1)

    val allCards = loadAllSet.values
      .filter(_.releaseDate.exists(LocalDate.parse(_).isBefore(nextWeek)))
      .filterNot(_.isOnlineOnly.getOrElse(false))
      .flatMap(edition => edition.cards.map(_ -> edition.copy(cards = Nil)))
      .groupBy { case (card, _) => (card.name, card.manaCost, card.`type`) }
      .values

    val result = allCards.map { groupedCards =>
      val groupedCardsSorted = groupedCards.toSeq.sortBy { case (_, edition) =>
        dateParser.parse(edition.releaseDate.getOrElse("1970-01-01"))
      }.map { case (card, edition) =>
        val urlTitle = StringUtils.normalize(card.name)
        val price = _price(urlTitle, edition.code, priceMap)
        (card, edition, price)
      }
      val cards = groupedCardsSorted.map(_._1)
      val editions = groupedCardsSorted.map(_._2)
      val prices = groupedCardsSorted.flatMap(_._3).flatMap(_.price)
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
      val hints = _hiddenHints(description)
      val urlTitle = StringUtils.normalize(firstCard.name)
      val rarities = cards.map(_.rarity.replace("timeshifted ", "")).distinct
      MTGCard(
        title = firstCard.name,
        altTitles = firstCard.names.map(_.filterNot(_ == firstCard.name)).getOrElse(Seq.empty),
        frenchTitle = cards.flatMap(_.foreignData).flatten.find(_.language == "French").flatMap(_.name),
        castingCost = castingCost,
        colors = _colors(castingCost, hints, firstCard.colors),
        dualColors = _dualColors(castingCost, firstCard.colors),
        tripleColors = _tripleColors(castingCost, firstCard.colors),
        convertedManaCost = firstCard.convertedManaCost.map(_.toInt).getOrElse(0),
        `type` = firstCard.`type`,
        description = firstCard.text.getOrElse(""),
        power = firstCard.power,
        toughness = firstCard.toughness,
        editions = editions.map(_.name).distinct,
        rarities = rarities,
        priceRanges = _priceRanges(prices),
        publications = groupedCardsSorted.map { case (card, edition, price) =>
          val rarity = card.rarity.replace("timeshifted ", "")
          val rarityCode = rarity.head.toString.toUpperCase
          val editionCode = edition.code.toUpperCase
          val stdEditionCode = Some(gathererMap.getOrElse(editionCode, editionCode)).filter { code =>
            code.length < 4 && !editionsCodeWithoutImage(code)
          }
          Publication(
            collectorNumber = card.number,
            edition = edition.name match {
              case name if name.contains(",") => name.replace(",", ":")
              case name => name
            },
            editionCode = editionCode,
            editionReleaseDate = Some(dateParser.parse(edition.releaseDate.getOrElse("1970-01-01"))),
            stdEditionCode = stdEditionCode,
            rarity = rarity,
            rarityCode = Some(rarityCode),
            image = card.multiverseId.map { multiverseId =>
              s"${URIs.pictureHost}/pics/$editionCode/$multiverseId-$urlTitle.jpg"
            },
            editionImage = stdEditionCode.map { code =>
              s"${URIs.pictureHost}/sets/$code/$rarityCode.gif"
            },
            price = price.flatMap(_.price),
            foilPrice = price.flatMap(_.foilPrice),
            block = edition.block,
            multiverseId = card.multiverseId
          )
        },
        abilities = _abilities(firstCard.name, description, abilities),
        formats = _formats(cards.head.legalities.map(processLegalities).getOrElse(Seq.empty), editions, formats, firstCard.name, rarities),
        artists = cards.map(_.artist).distinct,
        devotions = _devotions(Some(firstCard.`type`), castingCost),
        blocks = editions.flatMap(_.block).distinct,
        layout = firstCard.layout,
        loyalty = firstCard.loyalty,
        special = _special(firstCard.name, firstCard.`type`, description),
        land = _land(firstCard.`type`, description),
        ruling = firstCard.rulings.getOrElse(Nil).map(r => Ruling(r.date, r.text))
      )
    }.toSeq

    log.info(s"cards total before: ${allCards.size} / after ${result.size}")
    log.info(s"missing prices before ${prices.size} / after ${priceMap.size}")
    priceMap.keys.toSeq.map(_._2).groupBy(a => a).mapValues(_.size).toSeq.sortBy(-_._2).foreach(t => log.info(s"missing edition: $t"))
    priceMap.mapValues(_.card).toSeq.sortBy(_._1).foreach(t => log.info(s"missing card: $t"))

    result
  }

  private def processLegalities(data: Map[String, String]): Seq[MTGJsonLegality] =
    data
      .map { case (format, legality) => MTGJsonLegality(format, legality) }
      .toSeq

  private def _price(name: String,
                     code: String,
                     priceMap: mutable.Map[(String, String), ScrapedPrice]) = {
    val k = name -> priceCodeMap.getOrElse(code.toLowerCase, code.toLowerCase)
    priceMap.get(k).map { price =>
      priceMap remove k
      price
    }
  }

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
    "ME1" -> "MED",
  )

  val priceCodeMap: Map[String, String] = Map(
    // mtgjson -> mtggoldfish
    "inv" -> "in",
    "usg" -> "uz",
    "ody" -> "od",
    "7ed" -> "7e",
    "tmp" -> "te",
    "mmq" -> "mm",
    "mir" -> "mi",
    "vis" -> "vi",
    "wth" -> "wl",
    "p02" -> "po2",
    "apc" -> "ap",
    "sth" -> "st",
    "ulg" -> "ul",
    "uds" -> "ud",
    "pls" -> "ps",
    "exo" -> "ex",
    "pcy" -> "pr",
    "nem" -> "ne",
    "ppre" -> "prm-pre",
    "fnm" -> "prm-fnm",
    "jgp" -> "prm-jud",
    "g99" -> "prm-jud",
    //"pMGD" -> "prm-gdp",
    "pmei" -> "prm-med",
    "psus" -> "prm-jss",
    "mpr" -> "prm-mpr",
    "pr2" -> "prm-mpr",
    "pGTW" -> "prm-gwp",
    //"FRF_UGIN" -> "prm-ugf",
    "pwpn" -> "prm-wpn",
    "pLPA" -> "prm-lpc",
    "prel" -> "prm-rel",
    "pcmp" -> "prm-chp",
    "pgpx" -> "prm-gpp",
    "ppro" -> "prm-ptp",
    "HOP" -> "pc1",
    "mps" -> "ms2",
    "mp2" -> "ms3", // Masterpiece Series: Amonkhet Invocations
    "psdc" -> "prm-sdcc13",
    "pss3" -> "prm-ssp",
    "plpa" -> "prm-lpc",
    "pgru" -> "prm-gur",
    // prm-msc ?
    // prm-spo ?
    // prm-bab ?
  ) ++
    (1 to 18).map(i => f"f$i%02d" -> "prm-fnm").toMap ++
    (0 to 11).map(i => f"g$i%02d" -> "prm-jud").toMap ++
    (12 to 18).map(i => f"j$i%02d" -> "prm-jud").toMap ++
    (9 to 12).map(i => f"pwp$i%02d" -> "prm-wpn").toMap ++
    (3 to 11).map(i => f"p$i%02d" -> "prm-mpr").toMap ++
    (14 to 17).map(i => s"ps$i" -> s"prm-sdcc$i").toMap
}
