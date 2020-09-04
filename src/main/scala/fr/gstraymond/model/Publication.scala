package fr.gstraymond.model

case class Publication(
  collectorNumber: Option[String],
  edition: String,
  editionCode: String,
  editionReleaseDate: Option[Long],
  stdEditionCode: Option[String],
  rarity: String,
  rarityCode: Option[String],
  image: Option[String],
  editionImage: Option[String],
  price: Option[Double],
  foilPrice: Option[Double],
  mtgoPrice: Option[Double],
  mtgoFoilPrice: Option[Double],
  block: Option[String],
  multiverseId: Option[Long]
)
