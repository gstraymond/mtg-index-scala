package fr.gstraymond.model

import ai.x.play.json.Jsonx
import play.api.libs.json._

case class MTGCard(
  title: String,
  altTitles: Seq[String],
  frenchTitle: Option[String],
  castingCost: Option[String],
  colors: Seq[String],
  convertedManaCost: Int,
  `type`: String,
  description: String,
  power: Option[String],
  toughness: Option[String],
  editions: Seq[String],
  rarities: Seq[String],
  priceRanges: Seq[String],
  publications: Seq[Publication],
  abilities: Seq[String],
  formats: Seq[String],
  artists: Seq[String],
  devotions: Seq[Int],
  blocks: Seq[String],
  layout: String,
  loyalty: Option[Int],
  special: Seq[String],
  land: Seq[String]
)

object MTGCardFormat {
  implicit val publicationFormat = Json.format[Publication]
  implicit val mtgCardFormat = Jsonx.formatCaseClass[MTGCard]
}
