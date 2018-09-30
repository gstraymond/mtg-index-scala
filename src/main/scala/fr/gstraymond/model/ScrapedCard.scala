package fr.gstraymond.model

import play.api.libs.json.{Format, Json}

case class ScrapedCard(collectorNumber: String,
                       rarity: String,
                       artist: String,
                       edition: ScrapedEdition,
                       title: String,
                       frenchTitle: Option[String],
                       price: Option[Price] = None) {
  val uniqueId = s"${edition.code} - $collectorNumber"
}

case class Price(value: Option[Double],
                 foil: Option[Double])

object ScrapedCardFormat {
  import ScrapedEditionFormat._
  implicit val priceFormat: Format[Price] = Json.format
  implicit val scrapCardFormat: Format[ScrapedCard] = Json.format
}