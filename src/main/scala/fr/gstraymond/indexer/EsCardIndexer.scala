package fr.gstraymond.indexer

import com.github.plokhotnyuk.jsoniter_scala.core._
import fr.gstraymond.model.MTGCard
import fr.gstraymond.model.MTGCardFormat._

object EsCardIndexer extends EsIndexer[MTGCard] {

  override val index = "mtg"

  override def buildBody(group: Seq[MTGCard]): String =
    group.flatMap { card =>
      Seq(
        writeToString(Index(IndexId(getId(card)))),
        writeToString(card)
      )
    }.mkString("\n") + "\n"
}
