package fr.gstraymond.indexer

import fr.gstraymond.rules.model.Rules
import play.api.libs.json.Json

object EsRulesIndexer extends EsIndexer[Rules] {

  override val index = "mtg-rules"
  override val `type` = "all"

  override def buildBody(group: Seq[Rules]) = {
    group.flatMap { rules =>
      val indexJson = Json.obj("index" -> Json.obj("_id" -> "rules"))
      import fr.gstraymond.rules.model.RuleFormats._
      val rulesJson = Json.toJson(rules)

      val indexVersionJson = Json.obj("index" -> Json.obj("_id" -> "version"))
      val versionJson = Json.obj("filename" -> rules.filename)

      Seq(indexJson, rulesJson, indexVersionJson, versionJson).map(Json.stringify)
    }.mkString("\n") + "\n"
  }
}
