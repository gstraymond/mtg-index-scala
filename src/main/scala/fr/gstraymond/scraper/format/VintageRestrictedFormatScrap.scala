package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object VintageRestrictedFormatScrap extends FormatScrap {
  override val name = "restricted"
  override val path = "/Vintage"

  override def restrictedCards(doc: Document) = {
    doc
      .select("table").asScala
      .filter(_.select("ul a.autocardhref").asScala.nonEmpty).head
      .select("ul a.autocardhref").asScala.map(_.text())
  }
}
