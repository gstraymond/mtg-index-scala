package fr.gstraymond.model

import play.api.libs.json.{JsValue, Json}

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
                       legalities: Option[Seq[MTGJsonLegality]])

case class MTGJsonForeignName(language: String,
                              name: String)

case class MTGJsonLegality(format: String,
                           legality: String)

object MTGJsonFormats {
  implicit val mtgJsonLegalityFormat = Json.format[MTGJsonLegality]
  implicit val mtgJsonForeignNameFormat = Json.format[MTGJsonForeignName]
  implicit val mtgJsonCardFormat = Json.format[MTGJsonCard]
  implicit val mtgJsonEditionFormat = Json.format[MTGJsonEdition]
}
