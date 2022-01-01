package fr.gstraymond.model

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

case class ScrapedFormat(
    name: String,
    availableSets: Set[String],
    bannedCards: Set[String],
    restrictedCards: Set[String]
)

object ScrapedFormatFormat {
  implicit val ScrapedFormatCodec: JsonValueCodec[Seq[ScrapedFormat]] =
    JsonCodecMaker.make[Seq[ScrapedFormat]](CodecMakerConfig.withTransientEmpty(false))
}
