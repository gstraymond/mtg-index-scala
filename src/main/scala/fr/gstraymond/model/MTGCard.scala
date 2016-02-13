package fr.gstraymond.model

case class MTGCard(
  title: String,
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
  hiddenHints: Seq[String],
  devotions: Seq[String]
)
