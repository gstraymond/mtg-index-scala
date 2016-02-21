package fr.gstraymond.scraper

import fr.gstraymond.utils.{FileUtils, ZipUtils}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object OracleScraper extends YawgatogScraper {

  val path = "/resources/oracle/"

  def scrap(): Future[Unit] = {
    for {
      doc <- scrap(path)

      link = doc.select("div.content ul li a").asScala.head.attr("href")
      _ = log.info(s"oracle: $link")

      bytes <- get(s"$path$link")
    } yield {
      ZipUtils.unZip(bytes, FileUtils.oraclePath)
      ()
    }
  }
}
