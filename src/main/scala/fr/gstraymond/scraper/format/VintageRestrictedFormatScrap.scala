package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.jdk.CollectionConverters._

object VintageRestrictedFormatScrap extends FormatScrap {
  override val name = "restricted"
  override val path = "/Vintage"

  override def restrictedCards(doc: Document): Seq[String] = {
    doc
      .select("#mw-content-text ul")
      .asScala
      .filter(_.select("a.autocardhref").asScala.nonEmpty)(1)
      .select("a.autocardhref")
      .asScala
      .map(_.text())
      .toSeq
  }
}
