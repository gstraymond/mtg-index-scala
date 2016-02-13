package fr.gstraymond.model

import play.api.libs.json.Json

case class RawCard(
  title: Option[String],
  castingCost: Option[String],
  `type`: Option[String],
  powerToughness: Option[String],
  description: Seq[String],
  editionRarity: Seq[String]
)

object RawCardFormat {
  implicit val rawCardFormat = Json.format[RawCard]
}
