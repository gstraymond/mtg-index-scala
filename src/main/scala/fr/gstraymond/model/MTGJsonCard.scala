package fr.gstraymond.model

import play.api.libs.json.{Format, Json}

case class MTGJsonEdition(name: String,
                          code: String,
                          releaseDate: Option[String],
                          isOnlineOnly: Option[Boolean],
                          block: Option[String],
                          cards: Seq[MTGJsonCard])

case class MTGJsonCard(//uuid: String,
                       layout: String,
                       name: String,
                       names: Option[Seq[String]],
                       manaCost: Option[String],
                       convertedManaCost: Option[Double],
                       colors: Option[Seq[String]],
                       `type`: String,
                       //supertypes: Option[Seq[String]],
                       //types: Option[Seq[String]],
                       //subtypes: Option[Seq[String]],
                       rarity: String,
                       text: Option[String],
                       //flavorText: Option[String],
                       artist: Option[String],
                       number: Option[String],
                       power: Option[String],
                       toughness: Option[String],
                       loyalty: Option[String],
                       multiverseId: Option[Long],
                       foreignData: Option[Seq[MTGJsonForeignData]],
                       legalities: Option[Map[String, String]],
                       rulings: Option[Seq[MTGJsonRuling]])

case class MTGJsonForeignData(language: String,
                              name: Option[String])

case class MTGJsonLegality(format: String,
                           legality: String)

case class MTGJsonRuling(date: String,
                         text: String)

object MTGJsonFormats {
  implicit val mtgJsonRulingFormat: Format[MTGJsonRuling] = Json.format
  implicit val mtgJsonForeignDataFormat: Format[MTGJsonForeignData] = Json.format
  implicit val mtgJsonCardFormat: Format[MTGJsonCard] = Json.format
  implicit val mtgJsonEditionFormat: Format[MTGJsonEdition] = Json.format
}
