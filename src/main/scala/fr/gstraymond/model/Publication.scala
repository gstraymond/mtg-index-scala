package fr.gstraymond.model

import java.util.Date

case class Publication(
  collectorNumber: Option[String],
  edition: String,
  editionCode: String,
  editionReleaseDate: Option[Date],
  stdEditionCode: Option[String],
  rarity: String,
  rarityCode: Option[String],
  image: Option[String],
  editionImage: Option[String],
  price: Option[Double],
  foilPrice: Option[Double],
  block: Option[String],
  multiverseId: Option[Long]
)
