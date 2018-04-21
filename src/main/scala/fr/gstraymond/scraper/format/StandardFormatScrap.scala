package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object StandardFormatScrap extends FormatScrap {
  override val name = "Standard"
  override val path = "/Standard"

  override def bannedCards(doc: Document): Seq[String] =
    doc.select("#mw-content-text > ul:nth-child(16) > li > a")
      .asScala.map(_.text())

  override def currentRotation(doc: Document): Seq[String] =
    doc.select(".wikitable > tbody:nth-child(1) > tr > td > i > a")
      .asScala.map(_.text().split(" \\(").head)
}
