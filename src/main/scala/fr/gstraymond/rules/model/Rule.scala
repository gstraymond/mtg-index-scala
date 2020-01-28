package fr.gstraymond.rules.model

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

case class Rule(id: Option[String],
                text: String,
                links: Seq[RuleLink],
                level: Int)

case class RuleLink(id: String,
                    start: Int,
                    end: Int)

case class Rules(filename: String,
                 rules: Seq[Rule])

object RuleFormats {
  implicit val RulesCodec: JsonValueCodec[Rules] = JsonCodecMaker.make[Rules](CodecMakerConfig.withTransientEmpty(false))
}