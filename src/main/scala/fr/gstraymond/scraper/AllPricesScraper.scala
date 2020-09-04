package fr.gstraymond.scraper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import sys.process._
import fr.gstraymond.utils.FileUtils
import java.io.File

object AllPricesScraper extends MtgJsonScraper {

  val path = "/api/v5/AllPrices.json"

  def scrap: Future[Unit] = Future {
    val command = s"curl '${buildFullUrl(path)}'" #| 
      "jq ." #| 
      "egrep -v '              .*,'" #> 
      new File(s"${FileUtils.scrapPath}/AllPrices.json")
      
    println(s"""command: $command""")
    command.!
  }
}
