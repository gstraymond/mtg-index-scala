package fr.gstraymond.parser.field

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE
import java.time.temporal.ChronoUnit.YEARS

import fr.gstraymond.model.{MTGJsonEdition, MTGJsonLegality, ScrapedFormat}
import fr.gstraymond.utils.Log

trait FormatsField extends Log {

  val allFormats = Seq(
    "Vintage",
    "Commander",
    "Legacy",
    "Modern",
    "Standard"
  )

  private val STANDARD_EXP = LocalDate.now().minus(2, YEARS)

  def _formats(formats: Seq[MTGJsonLegality],
               editions: Seq[MTGJsonEdition],
               scrapedFormats: Seq[ScrapedFormat],
               title: String): Seq[String] = {

    val isNewEdition = editions.map(_.releaseDate).map(LocalDate.parse(_, ISO_DATE)).exists(_.isAfter(STANDARD_EXP))

    val standard =
      if (isNewEdition) {
        (scrapedFormats
          .filter(format => format.availableSets.isEmpty || format.availableSets.exists(editions.map(_.name).contains))
          .filterNot(_.bannedCards.contains(title))
          ++ scrapedFormats.find(_.name == "Modern").filterNot(_.bannedCards.contains(title)).toSeq) // if modern is not up to date
          .map(_.name.capitalize)
      } else Nil

    val legalities =
      formats
        .filter(l => allFormats.contains(l.format))
        .filterNot(_.legality == "Banned")

    val restricted = legalities.find(_.legality == "Restricted")

    // Bug: when scraping mtg salvation Modern: MTG 2015 doesn't contains core set
//    val standardModern = scrapedFormats.filter { format =>
//      format.availableSets.isEmpty || format.availableSets.exists(editions.contains)
//    }.map(_.name)

    (legalities.map(_.format) ++ Seq(restricted).flatten.map(_.legality) ++ standard).distinct
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
