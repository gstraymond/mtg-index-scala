package fr.gstraymond.model

import play.api.libs.json.{Format, JsValue, Json}

case class MTGJsonEdition(name: String,
                          code: String,
                          gathererCode: Option[String],
                          magicCardsInfoCode: Option[String],
                          releaseDate: String,
                          block: Option[String],
                          cards: Seq[MTGJsonCard])

case class MTGJsonCard(//id: String,
                       layout: String,
                       name: String,
                       names: Option[Seq[String]],
                       manaCost: Option[String],
                       cmc: Option[Double],
                       colors: Option[Seq[String]],
                       `type`: String,
                       //supertypes: Option[Seq[String]],
                       //types: Option[Seq[String]],
                       //subtypes: Option[Seq[String]],
                       rarity: String,
                       text: Option[String],
                       //flavor: Option[String],
                       artist: String,
                       number: Option[String],
                       power: Option[String],
                       toughness: Option[String],
                       loyalty: Option[JsValue], // string or int
                       multiverseid: Option[Long],
                       foreignNames: Option[Seq[MTGJsonForeignName]],
                       legalities: Option[Seq[MTGJsonLegality]],
                       rulings: Option[Seq[MTGJsonRuling]])

case class MTGJsonForeignName(language: String,
                              name: String)

case class MTGJsonLegality(format: String,
                           legality: String)

case class MTGJsonRuling(date: String,
                         text: String)

object MTGJsonFormats {
  implicit val mtgJsonRulingFormat: Format[MTGJsonRuling] = Json.format
  implicit val mtgJsonLegalityFormat: Format[MTGJsonLegality] = Json.format
  implicit val mtgJsonForeignNameFormat: Format[MTGJsonForeignName] = Json.format
  implicit val mtgJsonCardFormat: Format[MTGJsonCard] = Json.format
  implicit val mtgJsonEditionFormat: Format[MTGJsonEdition] = Json.format
}
