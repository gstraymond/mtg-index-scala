package fr.gstraymond.scraper.format

import org.jsoup.nodes.Document

trait FormatScrap {

  def name: String

  def path: String

  def currentRotation(doc: Document): Seq[String] = Seq.empty

  def bannedCards(doc: Document): Seq[String] = Seq.empty

  def restrictedCards(doc: Document): Seq[String] = Seq.empty
}
