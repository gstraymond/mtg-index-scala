package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

object LegacyFormatScrap extends FormatScrap {
  override val name = "legacy"
  override val path = "/Legacy"

  override def bannedCards(doc: Document): Seq[String] =
    doc.getTexts(".div-col > ul:nth-child(1) > li > a")
}
