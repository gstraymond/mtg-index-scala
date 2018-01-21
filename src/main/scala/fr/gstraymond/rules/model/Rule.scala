package fr.gstraymond.rules.model

import play.api.libs.json.{Format, Json}

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
  implicit val ruleLinkFormat: Format[RuleLink] = Json.format
  implicit val ruleFormat: Format[Rule] = Json.format
  implicit val rulesFormat: Format[Rules] = Json.format
}