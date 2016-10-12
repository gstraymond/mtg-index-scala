package fr.gstraymond.parser.field

import fr.gstraymond.model.ScrapedFormat

trait FormatsField {

  def _formats(formats: Seq[ScrapedFormat], `type`: Option[String], description: Seq[String], title: String, editionNames: Seq[String]) = {
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
