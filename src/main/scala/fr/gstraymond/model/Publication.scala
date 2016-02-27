package fr.gstraymond.model

import java.util.Date

case class Publication(
  collectorNumber: String,
  edition: String,
  editionCode: String,
  editionReleaseDate: Option[Date],
  stdEditionCode: String,
  rarity: String,
  rarityCode: String,
  image: String,
  editionImage: String,
  price: Option[Double]
)
