package fr.gstraymond.parser.field

import fr.gstraymond.model.MTGJsonEdition
import fr.gstraymond.model.MTGJsonLegality

import java.time.LocalDate

trait FormatsField {

  private val includedFormats = Set(
    "alchemy",
    "commander",
    "duel",
    "historic",
    "legacy",
    "modern",
    "pauper",
    "penny",
    "pioneer",
    "standard",
    "vintage"
  )

  def _formats(formats: Set[MTGJsonLegality], editions: Seq[MTGJsonEdition]): Seq[String] = {

    val legalities =
      formats
        .filter(l => includedFormats(l.format))
        .filterNot(_.legality == "Banned")
        .filterNot(_.legality == "Not Legal")

    val restricted = legalities.find(_.legality == "Restricted")

    val future =
      if formats.contains(MTGJsonLegality("future", "Legal")) &&
        editions.forall(_.releaseDate.exists(LocalDate.parse(_).isBefore(LocalDate.now)))
      then
        if formats.size == 1 then Seq("Vintage", "Commander", "Legacy", "Modern", "Pioneer", "Standard")
        else Seq("Standard")
      else Nil

    // Bug: when scraping mtg salvation Modern: MTG 2015 doesn't contains core set
    //    val standardModern = scrapedFormats.filter { format =>
    //      format.availableSets.isEmpty || format.availableSets.exists(editions.contains)
    //    }.map(_.name)

    (legalities.map {
      case MTGJsonLegality("duel", _) => "Duel Commander"
      case MTGJsonLegality(format, _) => format.capitalize
    } ++
      restricted.toSeq.map(_.legality) ++
      future).toList
  }
}
