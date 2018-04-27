package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

object CommanderFormatScrap extends FormatScrap {
  override val name = "commander"
  override val path = "/Vintage"

  override def bannedCards(doc: Document): Seq[String] =
    doc.getTexts("#mw-content-text > ul:nth-child(8) > li > a").drop(3) ++
      // Any card referencing ante
      // Any card with Conspiracy card type
      Seq(
        "description->playing for ante",
        "type->conspiracy"
      )
}
