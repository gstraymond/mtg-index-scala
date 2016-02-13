package fr.gstraymond.parser

import fr.gstraymond.model.{MTGCard, RawCard, ScrapedCard}
import fr.gstraymond.utils.{StringUtils, Log}

object CardConverter extends Log {

  def convert(rawCards: Seq[RawCard], scrapedCards: Seq[ScrapedCard]): Seq[MTGCard] = {
    val groupedScrapedCards = scrapedCards.groupBy(r => StringUtils.normalize(r.title))

    rawCards
      .filter(!_.`type`.contains("Vanguard"))
      .filter(!_.`type`.contains("Scheme"))
      .filter(!_.`type`.contains("Phenomenon"))
      .filter(!_.`type`.exists(_.startsWith("Plane -- ")))
      .filter(!_.`type`.exists(_.endsWith(" Scheme")))
      .foreach { rawCard =>
        val title = getTitle(rawCard)
        groupedScrapedCards.get(title).map { cards =>
          ""
        }.getOrElse {
          log.error(s"title not found: $title - $rawCard")
        }
      }

    ???
  }

  private def getTitle(rawCard: RawCard) = StringUtils.normalize {
    val title = rawCard.title.getOrElse("")
    rawCard.description match {
      case desc if desc.exists(_.contains("This is half of the split card")) =>
        val d = desc.find(_.contains("This is half of the split card")).get
        val baseTitle = d.substring(32, d.length - 2)
        s"$title (${baseTitle.replace(" // ", "/")})"
      case _ => title
    }
  }
}
