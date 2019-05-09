package fr.gstraymond.model

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import fr.gstraymond.constant.JsonConf

case class ScrapedFormat(
  name: String,
  availableSets: Set[String],
  bannedCards: Set[String],
  restrictedCards: Set[String]
)

object ScrapedFormatFormat {
  implicit val ScrapedFormatCodec: JsonValueCodec[Seq[ScrapedFormat]] = JsonCodecMaker.make[Seq[ScrapedFormat]](JsonConf.codecMakerConfig)
}

