package fr.gstraymond.parser.field

import java.time.LocalDate

import fr.gstraymond.model.{MTGJsonLegality, ScrapedFormat}

trait FormatsField {

  private val formatHackEnabled = LocalDate.now().getYear == 2017

  val allFormats = Seq(
    "Vintage",
    "Commander",
    "Legacy",
    "Modern",
    "Standard")

  def _formats(formats: Seq[MTGJsonLegality], editions: Seq[String]): Seq[String] = {
    val legalities =
      formats
        .filter(l => allFormats.contains(l.format))
        .filterNot(_.legality == "Banned")

    val restricted = legalities.find(_.legality == "Restricted")

    if (formatHackEnabled && editions.contains("Amonkhet")) {
      allFormats
    } else {
      legalities.map(_.format) ++ Seq(restricted).flatten.map(_.legality)
    }
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
