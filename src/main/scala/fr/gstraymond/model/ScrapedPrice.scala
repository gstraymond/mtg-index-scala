package fr.gstraymond.model

import play.api.libs.json.Json

case class ScrapedPrice(
  card: String,
  editionCode: String,
  editionName: String,
  price: Double,
  daily: Double,
  weekly: Double
)

object ScrapedPriceFormat {
  implicit val scrapedPriceFormat = Json.format[ScrapedPrice]
}
