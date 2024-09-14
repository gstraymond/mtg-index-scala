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
    price: Option[Float],
    foilPrice: Option[Float],
    mtgoPrice: Option[Float],
    mtgoFoilPrice: Option[Float],
    block: Option[String],
    multiverseId: Option[Long]
)
