package fr.gstraymond.model

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

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
  implicit val mtgJsonEditionFormat: JsonValueCodec[Map[String, MTGJsonEdition]] = JsonCodecMaker.make[Map[String, MTGJsonEdition]](CodecMakerConfig(transientEmpty = false))
}
