package fr.gstraymond.model

import java.util.Date

case class Publication(
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
