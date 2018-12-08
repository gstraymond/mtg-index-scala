package fr.gstraymond.scraper

import java.io.File

import fr.gstraymond.utils.{FileUtils, ZipUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AllSetScraper extends MtgJsonScraper {

  val path = "/v4/json/AllSets.json.zip"

  def scrap: Future[Unit] = {
    get(path).map { bytes =>
      val dir = new File(FileUtils.scrapPath)
      if (!dir.exists()) dir.mkdirs()
      ZipUtils.unZip(bytes, FileUtils.scrapPath)
      ()
    }
  }
}
