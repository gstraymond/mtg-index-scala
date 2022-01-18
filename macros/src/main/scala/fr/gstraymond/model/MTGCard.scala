package fr.gstraymond.model

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

case class MTGCard(
    title: String,
    altTitles: Seq[String],
    frenchTitle: Option[String],
    castingCost: Option[String],
    colors: Seq[String],
    dualColors: Seq[String],
    tripleColors: Seq[String],
    quadColors: Seq[String],
    convertedManaCost: Int,
    `type`: String,
    description: String,
    power: Option[String],
    toughness: Option[String],
    editions: Seq[String],
    rarities: Seq[String],
    priceRanges: Seq[String],
    minPaperPriceRange: Option[String],
    minMtgoPriceRange: Option[String],
    publications: Seq[Publication],
    abilities: Seq[String],
    formats: Seq[String],
    artists: Seq[String],
    devotions: Seq[Int],
    blocks: Seq[String],
    layout: String,
    loyalty: Option[String],
    special: Seq[String],
    land: Seq[String],
    ruling: Seq[Ruling]
)

case class Ruling(date: String, text: String)

object MTGCardFormat {
  implicit val MTGCardCodec: JsonValueCodec[MTGCard] =
    JsonCodecMaker.make[MTGCard](CodecMakerConfig.withTransientEmpty(false))
  implicit val MTGCardsCodec: JsonValueCodec[Seq[MTGCard]] =
    JsonCodecMaker.make[Seq[MTGCard]](CodecMakerConfig.withTransientEmpty(false))
}
