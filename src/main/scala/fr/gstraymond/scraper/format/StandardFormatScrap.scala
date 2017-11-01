package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object StandardFormatScrap extends FormatScrap {
  override val name = "Standard"
  override val path = "/Standard"

  override def bannedCards(doc: Document) = {
    doc
      .select("ul").asScala
      .filter(_.select("li a.autocardhref").asScala.nonEmpty).head
      .select("li a.autocardhref").asScala.map(_.text())
  }

  override def currentRotation(doc: Document) = {
    doc
      .select("div#mw-content-text ul").asScala(2)
      .select("a").asScala.map(_.text().split(" \\(").head)
  }
}
