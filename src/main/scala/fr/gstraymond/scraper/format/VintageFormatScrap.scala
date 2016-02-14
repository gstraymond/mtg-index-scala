package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object VintageFormatScrap extends FormatScrap {
  override val name = "vintage"
  override val path = "/Vintage"

  override def bannedCards(doc: Document) = {
    doc
      .select("ul").asScala
      .filter(_.select("a.autocardhref").asScala.nonEmpty).head
      .select("a.autocardhref").asScala.map(_.text()) ++
      // Any card referencing ante
      // Any card with Conspiracy card type
      Seq(
        "description->playing for ante",
        "type->conspiracy"
      )
  }
}
