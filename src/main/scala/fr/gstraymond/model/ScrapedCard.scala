package fr.gstraymond.model

import play.api.libs.json.Json

case class ScrapedCard(
  collectorNumber: String,
  rarity: String,
  artist: String,
  edition: String,
  title: String,
  frenchTitle: Option[String]) {

  val uniqueId = s"$edition - $collectorNumber"
}

object ScrapedCardFormat {
  implicit val scrapCardFormat = Json.format[ScrapedCard]
}