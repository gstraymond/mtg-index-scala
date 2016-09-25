package fr.gstraymond.parser

import fr.gstraymond.constant.Color._
import fr.gstraymond.constant.{Abilities, Color, URIs}
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
        def title = getTitle(rawCard)
        groupedScrapedCards.get(title).map { cards =>
          def castingCost = _cc(rawCard)
          def hints = _hiddenHints(rawCard.description)
          MTGCard(
            title = _title(cards),
            altTitles = Seq.empty,
            frenchTitle = _frenchTitle(cards),
            castingCost = castingCost,
            colors = _colors(castingCost, hints, None),
            convertedManaCost = _cmc(castingCost),
            `type` = _type(rawCard),
            description = _desc(rawCard),
            power = _power(rawCard),
            toughness = _toughness(rawCard),
            editions = _editions(cards),
            rarities = _rarities(cards),
            priceRanges = _priceRanges(cards.flatMap(_.price.map(_.value))),
            publications = _publications(cards),
            abilities = _abilities(rawCard.`type`, rawCard.description),
            formats = _formats(formats, rawCard.`type`, rawCard.description, scrapedCards.head.title, scrapedCards.map(_.edition.name)),
            artists = _artists(cards),
            devotions = _devotions(rawCard.`type`, castingCost),
            blocks = Seq.empty,
            layout = "",
            loyalty = None
          )
        }.orElse {
          log.error(s"title not found: $title - $rawCard")
          None
        }
      }
  }

  private val SPLIT_DESC = "of the split card "

  private def getTitle(rawCard: RawCard) = StringUtils.normalize {
    def title = rawCard.title.getOrElse("")
    rawCard.description.find(_.contains(SPLIT_DESC)) match {
      case Some(d) =>
        def baseTitle = d.split(SPLIT_DESC)(1).dropRight(2)
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

  def _colors(maybeCastingCost: Option[String], hints: Seq[String], additionalColors: Option[Seq[String]]) = {
    val uncoloredHint = hints.contains("Devoid (This card has no color.)")
    val cc = maybeCastingCost.getOrElse("") ++ additionalColors.map {
        _.flatMap { color =>
          ALL_COLORS_SYMBOLS.find(_.lbl == color).map(_.symbol)
        }.mkString(" ", " ", "")
      }.getOrElse("")

    cc -> uncoloredHint match {
      case (castingCost, false) if castingCost != "" =>
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
      case _ => Seq(UNCOLORED)
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

  def _priceRanges(prices: Seq[Double]) = prices.map {
    case p if p < 0.20 => "< 0.20$"
    case p if p >= 0.20 && p < 0.50 => "0.20$ .. 0.50$"
    case p if p >= 0.50 && p < 1 => "0.50$ .. 1$"
    case p if p >= 1 && p < 5 => "1$ .. 5$"
    case p if p >= 5 && p < 20 => "5$ .. 20$"
    case p if p >= 20 && p < 100 => "20$ .. 100$"
    case p if p >= 100 => "> 100$"
  }.distinct

  def _publications(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map { scrapedCard =>
    def title = StringUtils.normalize(scrapedCard.title)
    def rarityCode = scrapedCard.edition.stdEditionCode.map { _ =>
      scrapedCard.rarity.head.toUpper.toString
    }
    def editionImage = scrapedCard.edition.stdEditionCode.map { stdEditionCode =>
      s"${URIs.pictureHost}/sets/$stdEditionCode/${rarityCode.get}.gif"
    }

    Publication(
      collectorNumber = Some(scrapedCard.collectorNumber),
      edition = scrapedCard.edition.name,
      editionCode = scrapedCard.edition.code,
      editionReleaseDate = scrapedCard.edition.releaseDate,
      stdEditionCode = scrapedCard.edition.stdEditionCode,
      rarity = scrapedCard.rarity,
      rarityCode = rarityCode,
      image = Some(s"${URIs.pictureHost}/pics/${scrapedCard.edition.code}/${scrapedCard.collectorNumber}-$title.jpg"),
      editionImage = editionImage,
      price = scrapedCard.price.map(_.value),
      block = None,
      multiverseId = None
    )
  }.sortBy(_.editionReleaseDate.map(_.getTime).getOrElse(Long.MaxValue))

  def _abilities(`type`: Option[String], description: Seq[String]) = Abilities.LIST.filter(description.mkString(" ").contains) match {
    case Seq() if `type`.exists(_.contains("Creature")) && description.isEmpty => Seq("Vanilla")
    case abilities => abilities
  }

  def _formats(formats: Seq[ScrapedFormat], `type`: Option[String], description: Seq[String], title: String, editionNames: Seq[String]) = {
    formats.filter { format =>
      lazy val isInSet = format.availableSets.isEmpty || format.availableSets.exists(editionNames.contains)
      lazy val isBanned = format.bannedCards.exists {
        case banned if banned.startsWith("description->") =>
          val keyword = banned.split("description->")(1)
          description.mkString(" ").toLowerCase.contains(keyword)
        case banned if banned.startsWith("type->") =>
          val keyword = banned.split("type->")(1)
          `type`.getOrElse("").toLowerCase.contains(keyword)
        case banned => banned == title
      }
      lazy val isRestricted = format.restrictedCards.isEmpty || format.restrictedCards.contains(title)
      isInSet && !isBanned && isRestricted
    }.map {
      _.name
    }
  }

  def _artists(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map(_.artist).distinct

  def _hiddenHints(description: Seq [String]) = {
    description.collect {
      case desc@"Devoid (This card has no color.)" => Seq(desc)
      case desc if desc.contains("[") && desc.contains("]") =>
        desc.split("\\[")(1).split("\\]").head.split("\\.").toSeq
    }.flatten
  }

  def _devotions(`type`: Option[String], maybeCastingCost: Option[String]) = {
    val isPermanent = Seq("Instant", "Sorcery").forall { t =>
      `type`.exists { !_.contains(t) }
    }
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
