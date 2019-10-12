package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

import scala.jdk.CollectionConverters._

trait FormatScrap {

  def name: String

  def path: String

  def currentRotation(doc: Document): Seq[String] = Seq.empty

  def bannedCards(doc: Document): Seq[String] = Seq.empty

  def restrictedCards(doc: Document): Seq[String] = Seq.empty

  implicit class DocScrap(doc: Document) {
    def getTexts(cssPath: String): Seq[String] = {
      val result = doc.select(cssPath).asScala.map(_.text())
      assert(result.nonEmpty, s"no result for $name [$cssPath]")
      result.toSeq
    }
  }

}