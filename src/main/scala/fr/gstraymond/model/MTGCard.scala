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
  loyalty: Option[String],
  special: Seq[String],
  land: Seq[String],
  ruling: Seq[Ruling]
)

case class Ruling(date: String,
                  text: String)

object MTGCardFormat {
  implicit val rulingFormat: Format[Ruling] = Json.format
  implicit val publicationFormat: Format[Publication] = Json.format
  implicit val mtgCardFormat: Format[MTGCard] = Jsonx.formatCaseClass
}
