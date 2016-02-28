package fr.gstraymond.model

import java.util.Date

case class Publication(
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
