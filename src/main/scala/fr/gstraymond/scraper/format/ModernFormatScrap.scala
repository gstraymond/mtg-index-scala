package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object ModernFormatScrap extends FormatScrap {
  override val name = "Modern"
  override val path = "/Modern"

  override def bannedCards(doc: Document) = {
    doc
      .select("ul").asScala
      .filter(_.select("li a.autocardhref").asScala.nonEmpty).head
      .select("li a.autocardhref").asScala.map(_.text())
  }

  override def currentRotation(doc: Document) = {
    doc
      .select("div ul").asScala
      .filter(_.select("a.mw-redirect").asScala.nonEmpty).head
      .select("a").asScala.map(_.text())
  }
}
