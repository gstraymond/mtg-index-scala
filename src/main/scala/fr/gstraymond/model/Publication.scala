package fr.gstraymond.model

case class Publication(
  edition: String,
  editionCode: String,
  stdEditionCode: String,
  rarity: String,
  rarityCode: String,
  image: String,
  editionImage: String,
  price: Option[Double]
)
