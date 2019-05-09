package fr.gstraymond.model

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import fr.gstraymond.constant.JsonConf

case class ScrapedPrice(card: String,
                        editionCode: String,
                        editionName: String,
                        price: Option[Double],
                        foilPrice: Option[Double])

object ScrapedPriceFormat {
  implicit val ScrapedPriceCodec: JsonValueCodec[Seq[ScrapedPrice]] = JsonCodecMaker.make[Seq[ScrapedPrice]](JsonConf.codecMakerConfig)
}
