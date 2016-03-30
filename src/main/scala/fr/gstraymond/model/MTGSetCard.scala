package fr.gstraymond.model

import java.util.Date

import play.api.libs.json.Json

case class MTGSetCard(
  title: String,
  frenchTitle: Option[String],
  castingCost: Option[String],
  `type`: String,
  description: String,
  power: Option[String],
  toughness: Option[String],
  formats: Seq[String],
  artists: Seq[String],
  collectorNumber: String,
  edition: String,
  editionCode: String,
  editionReleaseDate: Option[Date],
  stdEditionCode: Option[String],
  rarity: String,
  rarityCode: Option[String],
  image: String,
  editionImage: Option[String],
  price: Option[Double]
)

object MTGSetCardFormat {
  implicit val mtgSetCardFormat = Json.format[MTGSetCard]
}

