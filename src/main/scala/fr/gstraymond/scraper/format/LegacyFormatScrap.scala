package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object LegacyFormatScrap extends FormatScrap {
  override val name = "legacy"
  override val path = "/Legacy"

  override def bannedCards(doc: Document) = {
    doc
      .select("div#mw-content-text").asScala
      .filter(_.select("ul a.autocardhref").asScala.nonEmpty).head
      .select("ul a.autocardhref").asScala.map(_.text())
  }
}
