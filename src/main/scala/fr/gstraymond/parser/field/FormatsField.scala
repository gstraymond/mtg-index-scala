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

  private val pauperRarities = Set(
    "Common",
    "Basic Land"
  )

  def _formats(formats: Seq[MTGJsonLegality],
               editions: Seq[MTGJsonEdition],
               scrapedFormats: Seq[ScrapedFormat],
               title: String,
               rarities: Seq[String]): Seq[String] = {

    val editionNames = editions.map(_.name.replace(" Core Set", "")).toSet

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

    val pauper = scrapedFormats
      .filter(f => f.name.toLowerCase == "pauper")
      .filter(_ => rarities.exists(pauperRarities))
      .filterNot(_.bannedCards(title))
      .map(_.name.capitalize)

    // Bug: when scraping mtg salvation Modern: MTG 2015 doesn't contains core set
//    val standardModern = scrapedFormats.filter { format =>
//      format.availableSets.isEmpty || format.availableSets.exists(editions.contains)
//    }.map(_.name)

    (oldLegalities.map(_.format) ++ restricted.toSeq.map(_.legality) ++ newLegalities ++ pauper).distinct
  }
}
