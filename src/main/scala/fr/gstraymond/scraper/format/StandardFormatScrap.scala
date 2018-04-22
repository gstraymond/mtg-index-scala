package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

object StandardFormatScrap extends FormatScrap {
  override val name = "Standard"
  override val path = "/Standard"

  override def bannedCards(doc: Document): Seq[String] =
    doc.getTexts("#mw-content-text > ul:nth-child(16) > li > a")

  override def currentRotation(doc: Document): Seq[String] =
    doc.getTexts(".wikitable > tbody:nth-child(1) > tr > td > i > a").map(_.split(" \\(").head)
}
