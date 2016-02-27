package fr.gstraymond.parser

import fr.gstraymond.constant.Color._
import fr.gstraymond.constant.{Abilities, Color}
import fr.gstraymond.model._
import fr.gstraymond.utils.{Log, StringUtils}

object CardConverter extends Log {

  def convert(rawCards: Seq[RawCard], scrapedCards: Seq[ScrapedCard], formats: Seq[ScrapedFormat]): Seq[MTGCard] = {
    val groupedScrapedCards = scrapedCards.groupBy { card =>
      StringUtils.normalize(card.title)
    }

    rawCards
      .filterNot(_.`type`.contains("Vanguard"))
      .filterNot(_.`type`.contains("Scheme"))
      .filterNot(_.`type`.contains("Phenomenon"))
      .filterNot(_.`type`.exists(_.startsWith("Plane -- ")))
      .filterNot(_.`type`.exists(_.endsWith(" Scheme")))
      .filterNot(_.editionRarity.exists(_.startsWith("ASTRAL-")))
      .flatMap { rawCard =>
        val title = getTitle(rawCard)
        groupedScrapedCards.get(title).map { cards =>
          val castingCost = _cc(rawCard)
          val hints = _hiddenHints(rawCard)
          MTGCard(
            title = _title(cards),
            frenchTitle = _frenchTitle(cards),
            castingCost = castingCost,
            colors = _colors(castingCost, hints),
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
            formats = _formats(rawCard, cards, formats),
            artists = _artists(cards),
            hiddenHints = hints,
            devotions = _devotions(rawCard, castingCost)
          )
        }.orElse {
          log.error(s"title not found: $title - $rawCard")
          None
        }
      }
  }

  private val SPLIT_DESC = "of the split card "

  private def getTitle(rawCard: RawCard) = StringUtils.normalize {
    val title = rawCard.title.getOrElse("")
    rawCard.description.find(_.contains(SPLIT_DESC)) match {
      case Some(d) =>
        val baseTitle = d.split(SPLIT_DESC)(1).dropRight(2)
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

  def _colors(maybeCastingCost: Option[String], hints: Seq[String]) = {
    maybeCastingCost.map { castingCost =>
      def find(colors: Seq[Color]) = colors.filter(c => castingCost.contains(c.symbol))

      val colorHint = hints.find(_.contains("color indicator")).getOrElse("")
      val hintSymbols = ONLY_COLORED_SYMBOLS.map(_.lbl).filter(colorHint.contains)

      val colorCount = Math.max(hintSymbols.size, find(ONLY_COLORED_SYMBOLS).size)

      val colorNumber = colorCount match {
        case 0 => Seq(UNCOLORED)
        case 1 => Seq(MONOCOLORED)
        case s => Seq(MULTICOLORED(s), GOLD)
      }

      val guild = if (GUILDS.exists(castingCost.contains)) Seq(GUILD) else Seq.empty

      val symbols = find(ALL_COLORS_SYMBOLS).map(_.lbl)

      colorNumber ++ guild ++ symbols ++ hintSymbols
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

  def _editions(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map(_.edition.name)

  def _rarities(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map(_.rarity).distinct

  def _priceRanges(scrapedCards: Seq[ScrapedCard]) = scrapedCards.flatMap(_.price.map(_.value)).map {
    case p if p < 0.20 => "< 0.20$"
    case p if p >= 0.20 && p < 0.50 => "0.20$ .. 0.50$"
    case p if p >= 0.50 && p < 1 => "0.50$ .. 1$"
    case p if p >= 1 && p < 5 => "1$ .. 5$"
    case p if p >= 5 && p < 20 => "5$ .. 20$"
    case p if p >= 20 && p < 100 => "20$ .. 100$"
    case p if p >= 100 => "> 100$"
  }.distinct

  private val pictureHost = "http://dl.dropboxusercontent.com/u/22449802/mtg"

  def _publications(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map { scrapedCard =>
    Publication(
      collectorNumber = scrapedCard.collectorNumber,
      edition = scrapedCard.edition.name,
      editionCode = scrapedCard.edition.code,
      editionReleaseDate = scrapedCard.edition.releaseDate,
      stdEditionCode = scrapedCard.edition.stdEditionCode.getOrElse(scrapedCard.edition.code).toUpperCase,
      rarity = scrapedCard.rarity,
      rarityCode = scrapedCard.rarity.head.toUpper.toString,
      image = s"$pictureHost/pics/${scrapedCard.edition.code}/${scrapedCard.collectorNumber}.jpg",
      editionImage = s"$pictureHost/sets/${scrapedCard.edition.code}/${scrapedCard.rarity}.jpg",
      price = scrapedCard.price.map(_.value)
    )
  }.sortBy(_.editionReleaseDate.map(_.getTime).getOrElse(Long.MaxValue))

  def _abilities(rawCard: RawCard) = Abilities.LIST.filter(rawCard.description.mkString(" ").contains) match {
    case Seq() if rawCard.`type`.contains("Creature") => Seq("Vanilla")
    case abilities => abilities
  }

  def _formats(rawCard: RawCard,scrapedCards: Seq[ScrapedCard], formats: Seq[ScrapedFormat]) = {
    formats.filter { format =>
      lazy val isInSet = format.availableSets.isEmpty || format.availableSets.exists(scrapedCards.map(_.edition.name).contains)
      lazy val isBanned = format.bannedCards.exists {
        case banned if banned.startsWith("description->") =>
          val keyword = banned.split("description->")(1)
          rawCard.description.mkString(" ").toLowerCase.contains(keyword)
        case banned if banned.startsWith("type->") =>
          val keyword = banned.split("type->")(1)
          rawCard.`type`.getOrElse("").toLowerCase.contains(keyword)
        case banned => banned == scrapedCards.head.title
      }
      lazy val isRestricted = format.restrictedCards.isEmpty || format.restrictedCards.contains(scrapedCards.head.title)
      isInSet && !isBanned && isRestricted
    }.map {
      _.name
    }
  }

  def _artists(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map(_.artist).distinct

  def _hiddenHints(rawCard: RawCard) = {
    rawCard.description.collect {
      case desc if desc.contains("[") && desc.contains("]") =>
        desc.split("\\[")(1).split("\\]").head.split("\\.")
    }.flatten
  }

  def _devotions(rawCard: RawCard, maybeCastingCost: Option[String]) = {
    val isPermanent = Seq("Instant", "Sorcery").forall(!rawCard.`type`.contains(_))
    isPermanent -> maybeCastingCost match {
      case (true, Some(castingCost)) =>
        ONLY_COLORED_SYMBOLS
          .map { color =>
            castingCost.split(" ").collect {
              case symbol if symbol.contains(color.symbol) && symbol.contains("/") => symbol.head.toString.toInt
              case symbol if symbol.contains(color.symbol) => 1
            }.sum
          }
          .distinct
          .filter(_ > 0)
      case _ => Seq.empty
    }
  }
}
