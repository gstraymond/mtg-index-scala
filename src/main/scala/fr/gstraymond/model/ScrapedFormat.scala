package fr.gstraymond.model

import play.api.libs.json.Json

case class ScrapedFormat(
  name: String,
  availableSets: Set[String],
  bannedCards: Set[String],
  restrictedCards: Set[String]
)

object ScrapedFormatFormat {
  implicit val scrapedFormatFormat = Json.format[ScrapedFormat]
}

