package fr.gstraymond.parser

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

object PriceModels {

  case class CardPrice(uuid: String, paper: Option[Price], online: Option[Price])

  case class Price(normal: Option[Double], foil: Option[Double])

  implicit val cardPriceFormat: JsonValueCodec[Seq[CardPrice]] =
    JsonCodecMaker.make(CodecMakerConfig.withTransientEmpty(false))
}
