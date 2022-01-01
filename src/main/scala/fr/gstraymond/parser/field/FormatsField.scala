package fr.gstraymond.parser.field

import fr.gstraymond.model.MTGJsonEdition
import fr.gstraymond.model.MTGJsonLegality

import java.time.LocalDate

trait FormatsField:

  private val includedFormats = Set(
    "vintage",
    "commander",
    "legacy",
    "modern",
    "pauper",
    "pioneer",
    "standard",
    "penny"
  )

  def _formats(formats: Seq[MTGJsonLegality], editions: Seq[MTGJsonEdition]): Seq[String] =

    val legalities =
      formats
        .filter(l => includedFormats(l.format))
        .filterNot(_.legality == "Banned")
        .filterNot(_.legality == "Not Legal")

    val restricted = legalities.find(_.legality == "Restricted")

    val future =
      if
        formats.contains(MTGJsonLegality("future", "Legal")) &&
        editions.forall(_.releaseDate.exists(LocalDate.parse(_).isBefore(LocalDate.now)))
      then
        if formats.length == 1 then Seq("Vintage", "Commander", "Legacy", "Modern", "Pioneer", "Standard")
        else Seq("Standard")
      else Nil

    // Bug: when scraping mtg salvation Modern: MTG 2015 doesn't contains core set
    //    val standardModern = scrapedFormats.filter { format =>
    //      format.availableSets.isEmpty || format.availableSets.exists(editions.contains)
    //    }.map(_.name)

    (legalities.map(_.format.capitalize) ++ restricted.toSeq.map(_.legality) ++ future).distinct
