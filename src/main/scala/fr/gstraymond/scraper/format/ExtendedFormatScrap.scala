package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.jdk.CollectionConverters._

object ExtendedFormatScrap extends FormatScrap {
  override val name = "extended"
  override val path = "/Extended"

  override def bannedCards(doc: Document): Seq[String] =
    doc
      .select("ul")
      .asScala
      .filter(_.select("li a.autocardhref").asScala.nonEmpty)
      .head
      .select("li a.autocardhref")
      .asScala
      .map(_.text())
      .toSeq

  override def currentRotation(doc: Document): Seq[String] =
    doc
      .select("table.wikitable")
      .asScala
      .head
      .select("tr")
      .asScala
      .filter(_.select("td").asScala.nonEmpty)
      .map(_.select("td").asScala.head.text())
      .toSeq
}
