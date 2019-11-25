package fr.gstraymond.parser.field

import java.time.LocalDate

import fr.gstraymond.model.{MTGJsonEdition, MTGJsonLegality}
import fr.gstraymond.utils.Log

trait FormatsField extends Log {

  private val includedFormats = Set(
    "vintage", "commander", "legacy", "modern", "pauper", "pioneer", "standard"
  )

  def _formats(formats: Seq[MTGJsonLegality],
               editions: Seq[MTGJsonEdition],
               title: String,
               rarities: Seq[String]): Seq[String] = {

    val legalities =
      formats
        .filter(l => includedFormats(l.format))
        .filterNot(_.legality == "Banned")
        .filterNot(_.legality == "Not Legal")

    val restricted = legalities.find(_.legality == "Restricted")

    val future = if (formats.contains(MTGJsonLegality("future", "Legal")) &&
      editions.forall(_.releaseDate.exists(LocalDate.parse(_).isBefore(LocalDate.now)))) {
      if (formats.length == 1) Seq("Vintage", "Commander", "Legacy", "Modern", "Pioneer", "Standard")
      else Seq("Standard")
    } else Nil

    // Bug: when scraping mtg salvation Modern: MTG 2015 doesn't contains core set
    //    val standardModern = scrapedFormats.filter { format =>
    //      format.availableSets.isEmpty || format.availableSets.exists(editions.contains)
    //    }.map(_.name)

    (legalities.map(_.format.capitalize) ++ restricted.toSeq.map(_.legality) ++ future).distinct
  }
}
