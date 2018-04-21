package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object ModernFormatScrap extends FormatScrap {
  override val name = "Modern"
  override val path = "/Modern"

  override def bannedCards(doc: Document): Seq[String] =
    doc.select("div.div-col:nth-child(17) > ul:nth-child(1) > li > a")
      .asScala.map(_.text())

  override def currentRotation(doc: Document): Seq[String] =
    doc.select("div.div-col:nth-child(10) > ul:nth-child(1) > li > i > a")
      .asScala.map(_.text())
}
