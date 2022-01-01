package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

object ModernFormatScrap extends FormatScrap:
  override val name = "Modern"
  override val path = "/Modern"

  override def bannedCards(doc: Document): Seq[String] =
    doc.getTexts("div.div-col:nth-child(17) > ul:nth-child(1) > li > a")

  override def currentRotation(doc: Document): Seq[String] =
    doc.getTexts("div.div-col:nth-child(10) > ul:nth-child(1) > li > i > a")
