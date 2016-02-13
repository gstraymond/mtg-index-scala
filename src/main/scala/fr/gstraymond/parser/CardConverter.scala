package fr.gstraymond.parser

import fr.gstraymond.constant.Color
import fr.gstraymond.constant.Color._
import fr.gstraymond.model.{MTGCard, RawCard, ScrapedCard}
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
            `type` = ???,
            description = ???,
            power = ???,
            toughness = ???,
            editions = ???,
            rarities = ???,
            priceRanges = ???,
            publications = ???,
            abilities = ???,
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

  def _title(scrapedCards: Seq[ScrapedCard]) =
    scrapedCards.head.title

  def _frenchTitle(scrapedCards: Seq[ScrapedCard]) =
    scrapedCards.flatMap(_.frenchTitle).headOption

  def _cc(rawCard: RawCard): Option[String] = rawCard.castingCost.map { cc =>
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

  def _colors(maybeCastingCost: Option[String]): Seq[String] = {
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

  def _cmc(maybeCastingCost: Option[String]): Int = {
    maybeCastingCost.map {
      _.split(" ")
        .filter(_ != X.lbl)
        .map {
          case number if number.forall(_.isDigit) => Integer.parseInt(number)
          case spec if spec.head.isDigit => Integer.parseInt(spec.head.toString)
          case _ => 1
        }
        .sum
    }.getOrElse {
      0
    }
  }
}
