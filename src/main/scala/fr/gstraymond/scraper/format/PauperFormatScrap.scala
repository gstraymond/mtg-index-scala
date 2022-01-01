package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

object PauperFormatScrap extends FormatScrap:
  override val name = "pauper"
  override val path = "/Pauper"

  override def bannedCards(doc: Document): Seq[String] =
    doc.getTexts(".mw-parser-output > ul:nth-child(24) > li")
