package fr.gstraymond.indexer

import com.github.plokhotnyuk.jsoniter_scala.core.*
import fr.gstraymond.indexer.IndexerModels.*
import fr.gstraymond.rules.model.RuleFormats.*
import fr.gstraymond.rules.model.Rules

object EsRulesIndexer extends EsIndexer[Rules] {

  override val index = "mtg-rules"

  override def buildBody(group: Seq[Rules]): String =
    group
      .flatMap { rules =>
        val indexJson        = Index(IndexId("rules"))
        val indexVersionJson = Index(IndexId("version"))
        val versionJson      = RuleVersion(rules.filename)

        Seq(
          writeToString(indexJson),
          writeToString(rules),
          writeToString(indexVersionJson),
          writeToString(versionJson)
        )
      }
      .mkString("\n") + "\n"
}
