package fr.gstraymond.model

import play.api.libs.json.Json

case class ScrapedCard(
  collectorNumber: String,
  rarity: String,
  artist: String,
  edition: ScrapedEdition,
  title: String,
  frenchTitle: Option[String],
  price: Option[Price] = None) {

  val uniqueId = s"${edition.code} - $collectorNumber"
}

case class Price(
  value: Double,
  daily: Double,
  weekly: Double
)

object ScrapedCardFormat {
  import ScrapedEditionFormat._
  implicit val priceFormat = Json.format[Price]
  implicit val scrapCardFormat = Json.format[ScrapedCard]
}