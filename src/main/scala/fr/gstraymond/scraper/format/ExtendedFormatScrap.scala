package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object ExtendedFormatScrap extends FormatScrap {
  override val name = "extended"
  override val path = "/Extended"

  override def bannedCards(doc: Document) = {
    doc
      .select("ul").asScala
      .filter(_.select("li a.autocardhref").asScala.nonEmpty).head
      .select("li a.autocardhref").asScala.map(_.text())
  }

  override def currentRotation(doc: Document) = {
    doc
      .select("table.wikitable").asScala.head
      .select("tr").asScala
      .filter(_.select("td").asScala.nonEmpty)
      .map(_.select("td").asScala.head.text())
  }
}
