package fr.gstraymond.parser

import fr.gstraymond.constant.{Abilities, Color}
import fr.gstraymond.constant.Color._
import fr.gstraymond.model.{Publication, MTGCard, RawCard, ScrapedCard}
import fr.gstraymond.utils.{Log, StringUtils}

object CardConverter extends Log {

  def convert(rawCards: Seq[RawCard], scrapedCards: Seq[ScrapedCard]): Seq[MTGCard] = {
    val groupedScrapedCards = scrapedCards.groupBy { card =>
      StringUtils.normalize(card.title)
    }

    rawCards
      .filter(!_.`type`.contains("Vanguard"))
      .filter(!_.`type`.contains("Scheme"))
      .filter(!_.`type`.contains("Phenomenon"))
      .filter(!_.`type`.exists(_.startsWith("Plane -- ")))
      .filter(!_.`type`.exists(_.endsWith(" Scheme")))
      .flatMap { rawCard =>
        val title = getTitle(rawCard)
        groupedScrapedCards.get(title).map { cards =>
          val castingCost = _cc(rawCard)
          MTGCard(
            title = _title(cards),
            frenchTitle = _frenchTitle(cards),
            castingCost = castingCost,
            colors = _colors(castingCost),
            convertedManaCost = _cmc(castingCost),
            `type` = _type(rawCard),
            description = _desc(rawCard),
            power = _power(rawCard),
            toughness = _toughness(rawCard),
            editions = _editions(cards),
            rarities = _rarities(cards),
            priceRanges = _priceRanges(cards),
            publications = _publications(cards),
            abilities = _abilities(rawCard),
            formats = ???,
            artists = ???,
            hiddenHints = ???,
            devotions = ???
          )
        }.orElse {
          log.error(s"title not found: $title - $rawCard")
          None
        }
      }
  }

  private val SPLIT_DESC = "This is half of the split card"

  private def getTitle(rawCard: RawCard) = StringUtils.normalize {
    val title = rawCard.title.getOrElse("")
    rawCard.description.find(_.contains(SPLIT_DESC)) match {
      case Some(d) =>
        val baseTitle = d.substring(32, d.length - 2)
        s"$title (${baseTitle.replace(" // ", "/")})"
      case _ => title
    }
  }

  def _title(scrapedCards: Seq[ScrapedCard]) = scrapedCards.head.title

  def _frenchTitle(scrapedCards: Seq[ScrapedCard]) = scrapedCards.flatMap(_.frenchTitle).headOption

  def _cc(rawCard: RawCard) = rawCard.castingCost.map { cc =>
    cc.toSeq.foldLeft("" -> false) { case ((acc, inP), char) =>
      val tuple = char match {
        case '(' => " " -> true
        case ')' => "" -> false
        case '/' => "" -> inP
        case c if c.isDigit && acc.lastOption.exists(_.isDigit) => c -> inP
        case c if inP && c.isDigit => s"$c/" -> inP
        case c if inP => c -> inP
        case c => s" $c" -> inP
      }

      tuple match {
        case (c, i) => acc + c -> i
      }
    }._1.trim.toUpperCase()
  }

  def _colors(maybeCastingCost: Option[String]) = {
    maybeCastingCost.map { castingCost =>
      def find(colors: Seq[Color]) = colors.filter(c => castingCost.contains(c.symbol))

      val colorNumber = find(ALL_COLORS_SYMBOLS.filter(_.colored)).size match {
        case 0 => Seq(UNCOLORED)
        case 1 => Seq(MONOCOLORED)
        case s => Seq(MULTICOLORED(s), GOLD)
      }

      val guild = if (GUILDS.exists(castingCost.contains)) Seq(GUILD) else Seq.empty

      colorNumber ++ guild ++ find(ALL_COLORS_SYMBOLS).map(_.lbl)
    }.getOrElse {
      Seq(UNCOLORED)
    }
  }

  def _cmc(maybeCastingCost: Option[String]) = {
    maybeCastingCost.map {
      _.split(" ")
        .filter(_ != X.lbl)
        .map {
          case number if number.forall(_.isDigit) => number.toInt
          case spec if spec.head.isDigit => spec.head.toString.toInt
          case _ => 1
        }
        .sum
    }.getOrElse {
      0
    }
  }

  def _type(rawCard: RawCard) = rawCard.`type`.getOrElse {
    throw new RuntimeException(s"$rawCard has not type :/")
  }

  def _desc(rawCard: RawCard) = rawCard.description.mkString("\n")

  def _power(rawCard: RawCard) = rawCard.powerToughness.map(_.split("/")(0))

  def _toughness(rawCard: RawCard) = rawCard.powerToughness.map(_.split("/")(1))

  def _editions(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map(_.editionName)

  def _rarities(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map(_.rarity).distinct

  def _priceRanges(scrapedCards: Seq[ScrapedCard]) = scrapedCards.flatMap(_.price.map(_.value)).map {
    case p if p < 0.20 => "< 0.20$"
    case p if p >= 0.20 && p < 0.50 => "0.20$ .. 0.50$"
    case p if p >= 0.50 && p < 1 => "0.50$ .. 1$"
    case p if p >= 1 && p < 5 => "1$ .. 5$"
    case p if p >= 5 && p < 20 => "5$ .. 20$"
    case p if p >= 20 && p < 100 => "20$ .. 100$"
    case p if p >= 100 => "> 100$"
  }

  def _publications(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map { scrapedCard =>
    Publication(
      edition = scrapedCard.editionName,
      editionCode = scrapedCard.editionCode,
      stdEditionCode = "TODO",
      rarity = scrapedCard.rarity,
      rarityCode = scrapedCard.rarity.head.toUpper.toString,
      image = s"http://magiccards.info/scans/en/${scrapedCard.editionCode}/${scrapedCard.collectorNumber}.jpg",
      editionImage = "TODO",
      price = scrapedCard.price.map(_.value)
    )
  }

  def _abilities(rawCard: RawCard) = Abilities.LIST.filter(rawCard.description.mkString("").contains) match {
    case Seq() if rawCard.`type`.contains("Creature") => Seq("Vanilla")
    case abilities => abilities
  }
}
