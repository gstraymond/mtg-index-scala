package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object VintageFormatScrap extends FormatScrap {
  override val name = "vintage"
  override val path = "/Vintage"

  override def bannedCards(doc: Document): Seq[String] =
    doc.select("#mw-content-text > ul:nth-child(8) > li > a")
      .asScala.drop(3).map(_.text()) ++
      // Any card referencing ante
      // Any card with Conspiracy card type
      Seq(
        "description->playing for ante",
        "type->conspiracy"
      )
}
