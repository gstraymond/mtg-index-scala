package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object LegacyFormatScrap extends FormatScrap {
  override val name = "legacy"
  override val path = "/Legacy"

  override def bannedCards(doc: Document): Seq[String] =
    doc.select(".div-col > ul:nth-child(1) > li > a")
      .asScala.map(_.text())
}
