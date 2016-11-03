package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._

object StandardFormatScrap extends FormatScrap {
  override val name = "standard"
  override val path = "/Standard"

  override def bannedCards(doc: Document) = {
    doc
      .select("a.autocardhref").asScala.map(_.text())
  }

  override def currentRotation(doc: Document) = {
    doc
      .select("ul").asScala(4)
      .select("li").asScala.map(_.text().split(" \\(").head)
  }
}
