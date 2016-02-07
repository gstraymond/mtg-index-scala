package fr.gstraymond.model

import play.api.libs.json.Json

case class ScrapedFormat(
  name: String,
  availableSets: Seq[String],
  bannedCards: Seq[String],
  restrictedCards: Seq[String]
)

object ScrapedFormatFormat {
  implicit val scrapedFormatFormat = Json.format[ScrapedFormat]
}

