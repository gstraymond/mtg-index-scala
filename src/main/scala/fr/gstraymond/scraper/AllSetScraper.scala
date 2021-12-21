package fr.gstraymond.scraper

import fr.gstraymond.utils.FileUtils
import fr.gstraymond.utils.ZipUtils

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AllSetScraper extends MtgJsonScraper {

  val path = "/api/v5/AllPrintings.json.zip"

  def scrap: Future[Unit] = {
    get(path).map { bytes =>
      val dir = new File(FileUtils.scrapPath)
      if (!dir.exists()) dir.mkdirs()
      ZipUtils.unZip(bytes, FileUtils.scrapPath)
      ()
    }
  }
}
