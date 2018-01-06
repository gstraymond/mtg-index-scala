package fr.gstraymond.rules.model

import play.api.libs.json.{Format, Json}

case class Rule(id: Option[String],
                text: String,
                link: Option[String],
                level: Int)

case class Rules(rules: Seq[Rule])

object RuleFormats {
  implicit val ruleFormat: Format[Rule] = Json.format
  implicit val rulesFormat: Format[Rules] = Json.format
}