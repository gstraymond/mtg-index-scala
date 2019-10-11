package fr.gstraymond.parser.field

import java.time.LocalDate

import fr.gstraymond.model.{MTGJsonEdition, MTGJsonLegality, ScrapedFormat}
import fr.gstraymond.utils.Log

trait FormatsField extends Log {

  private val oldFormats = Set(
    "Vintage",
    "Commander",
    "Legacy",
  )

  private val excludedFormats = Set(
    "brawl", "duel", "frontier", "penny", "future", "historic"
  )

  private val newFormats = Set(
    "modern",
    "standard",
  )

  private val pauperRarities = Set(
    "common"
  )

  def _formats(formats: Seq[MTGJsonLegality],
               editions: Seq[MTGJsonEdition],
               scrapedFormats: Seq[ScrapedFormat],
               title: String,
               rarities: Seq[String]): Seq[String] = {

    //    val editionNames = editions.map(_.name.replace(" Core Set", "")).toSet

    //    val newLegalities =
    //        scrapedFormats
    //          .filter(f => newFormats(f.name.toLowerCase))
    //          .filter(format => format.availableSets.isEmpty || format.availableSets.exists(editionNames))
    //          .filterNot(_.bannedCards(title))
    //          .map(_.name.capitalize)

    val oldLegalities =
      formats
        //.filter(l => oldFormats(l.format))
        .filterNot(l => excludedFormats(l.format))
        .filterNot(_.legality == "Banned")
        .filterNot(_.legality == "Not Legal")

    val restricted = oldLegalities.find(_.legality == "Restricted")

    val pauper = scrapedFormats
      .filter(f => f.name.toLowerCase == "pauper")
      .filter(_ => rarities.exists(pauperRarities))
      .filterNot(_.bannedCards(title))
      .map(_.name.capitalize)

    val future = if (formats.contains(MTGJsonLegality("future", "Legal")) &&
      editions.forall(_.releaseDate.exists(LocalDate.parse(_).isBefore(LocalDate.now)))) {
      if (formats.length == 1) Seq("Vintage", "Commander", "Legacy", "Modern", "Standard")
      else Seq("Standard")
    } else Nil

    // Bug: when scraping mtg salvation Modern: MTG 2015 doesn't contains core set
    //    val standardModern = scrapedFormats.filter { format =>
    //      format.availableSets.isEmpty || format.availableSets.exists(editions.contains)
    //    }.map(_.name)

    (oldLegalities.map(_.format.capitalize) ++ restricted.toSeq.map(_.legality) /*++ newLegalities*/ ++ pauper ++ future).distinct
  }
}
