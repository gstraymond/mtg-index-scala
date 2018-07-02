package fr.gstraymond.parser

import fr.gstraymond.constant.Color._
import fr.gstraymond.constant.URIs
import fr.gstraymond.model._
import fr.gstraymond.parser.field._
import fr.gstraymond.utils.{Log, StringUtils}

object CardConverter extends Log
  with AbilitiesField
  with ColorField
  with DevotionsField
  with FormatsField
  with HiddenHintsField
  with LandField
  with PriceRangesField
  with SpecialField {

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
          val hints = _hiddenHints(rawCard.description)
          val title = _title(cards)
          val `type` = _type(rawCard)
          MTGCard(
            title = title,
            altTitles = Seq.empty,
            frenchTitle = _frenchTitle(cards),
            castingCost = castingCost,
            colors = _colors(castingCost, hints, None),
            dualColors = Nil,
            tripleColors = Nil,
            convertedManaCost = _cmc(castingCost),
            `type` = `type`,
            description = _desc(rawCard),
            power = _power(rawCard),
            toughness = _toughness(rawCard),
            editions = _editions(cards),
            rarities = _rarities(cards),
            priceRanges = _priceRanges(cards.flatMap(_.price.map(_.value))),
            publications = _publications(cards),
            abilities = _abilities(rawCard.title.getOrElse(""), rawCard.description, Nil), // FIXME
            formats = _old_formats(formats, rawCard.`type`, rawCard.description, title, scrapedCards.map(_.edition.name)),
            artists = _artists(cards),
            devotions = _devotions(rawCard.`type`, castingCost),
            blocks = Seq.empty,
            layout = "",
            loyalty = None,
            special = _special(title, `type`, rawCard.description),
            land = _land(`type`, rawCard.description),
            ruling = Nil
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

  def _artists(scrapedCards: Seq[ScrapedCard]) = scrapedCards.map(_.artist).distinct
}
