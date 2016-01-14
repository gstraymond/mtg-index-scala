package fr.gstraymond.model

case class RawCard(
  title: Option[String],
  castingCost: Option[String],
  `type`: Option[String],
  powerToughness: Option[String],
  description: Seq[String],
  editionRarity: Option[String]
)
