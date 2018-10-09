package fr.gstraymond.parser.field

import fr.gstraymond.model.{MTGJsonEdition, MTGJsonLegality, ScrapedFormat}
import fr.gstraymond.utils.Log

trait FormatsField extends Log {

  val oldFormats = Set(
    "Vintage",
    "Commander",
    "Legacy",
  )

  val newFormats = Set(
    "modern",
    "standard",
  )

  def _formats(formats: Seq[MTGJsonLegality],
               editions: Seq[MTGJsonEdition],
               scrapedFormats: Seq[ScrapedFormat],
               title: String): Seq[String] = {

    val editionNames = editions.map(_.name).toSet

    val newLegalities =
        scrapedFormats
          .filter(f => newFormats(f.name.toLowerCase))
          .filter(format => format.availableSets.isEmpty || format.availableSets.exists(editionNames))
          .filterNot(_.bannedCards(title))
          .map(_.name.capitalize)

    val oldLegalities =
      formats
        .filter(l => oldFormats(l.format))
        .filterNot(_.legality == "Banned")

    val restricted = oldLegalities.find(_.legality == "Restricted")

    // Bug: when scraping mtg salvation Modern: MTG 2015 doesn't contains core set
//    val standardModern = scrapedFormats.filter { format =>
//      format.availableSets.isEmpty || format.availableSets.exists(editions.contains)
//    }.map(_.name)

    (oldLegalities.map(_.format) ++ Seq(restricted).flatten.map(_.legality) ++ newLegalities).distinct
  }

  def _old_formats(formats: Seq[ScrapedFormat], `type`: Option[String], description: Seq[String], title: String, editionNames: Seq[String]) = {
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
}
