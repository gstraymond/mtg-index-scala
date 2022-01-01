package fr.gstraymond.model

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import scala.annotation.nowarn

case class MTGJsonAllPrintings(data: Map[String, MTGJsonEdition])

case class MTGJsonEdition(
    name: String,
    code: String,
    releaseDate: Option[String],
    isOnlineOnly: Option[Boolean],
    block: Option[String],
    cards: Seq[MTGJsonCard]
)

case class MTGJsonCard(
    uuid: String,
    layout: String,
    name: String,
    faceName: Option[String],
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
    identifiers: MTGJsonIdentifiers,
    foreignData: Option[Seq[MTGJsonForeignData]],
    legalities: Option[Map[String, String]],
    rulings: Option[Seq[MTGJsonRuling]]
)

case class MTGJsonIdentifiers(multiverseId: Option[String])

case class MTGJsonForeignData(language: String, name: Option[String])

case class MTGJsonLegality(format: String, legality: String)

case class MTGJsonRuling(date: String, text: String)

case class MTGJsonAllPrices(data: Map[String, MTGJsonPrice])

case class MTGJsonPrice(paper: Option[MTGJsonPricePaper], mtgo: Option[MTGJsonPriceOnline])

case class MTGJsonPricePaper(cardkingdom: Option[MTGJsonPriceProvider], tcgplayer: Option[MTGJsonPriceProvider])

case class MTGJsonPriceOnline(cardhoarder: MTGJsonPriceProvider)

case class MTGJsonPriceProvider(retail: Option[MTGJsonRetail])

case class MTGJsonRetail(
    normal: Option[Map[String, Double]],
    foil: Option[Map[String, Double]]
)

object MTGJsonFormats {
  implicit val mtgJsonEditionFormat: JsonValueCodec[MTGJsonAllPrintings] =
    JsonCodecMaker.make(CodecMakerConfig.withTransientEmpty(false))

  @nowarn
  implicit val mtgJsonAllPricesFormat: JsonValueCodec[MTGJsonAllPrices] =
    JsonCodecMaker.make(CodecMakerConfig.withTransientEmpty(false).withMapMaxInsertNumber(100000))
}
