package fr.gstraymond.model

import play.api.libs.json.{Format, Json}

case class ScrapedPrice(card: String,
                        editionCode: String,
                        editionName: String,
                        price: Double,
                        foilPrice: Option[Double])

object ScrapedPriceFormat {
  implicit val scrapedPriceFormat: Format[ScrapedPrice] = Json.format
}
