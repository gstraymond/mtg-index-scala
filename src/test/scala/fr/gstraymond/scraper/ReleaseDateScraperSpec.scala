package fr.gstraymond.scraper

import java.util.concurrent.TimeUnit

import fr.gstraymond.model.ScrapedEdition
import org.junit.Ignore
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import scala.concurrent.duration.Duration

@Ignore
@RunWith(classOf[JUnitRunner])
class ReleaseDateScraperSpec(implicit ee: ExecutionEnv) extends Specification {

  "release date scraper" should {
    "scrap" in {
      val editions = Seq(
        ScrapedEdition("", "Oath of the Gatewatch", None),
        ScrapedEdition("", "Battle for Zendikar", None)
      )

      ReleaseDateScraper.scrap(editions) must be_==("").awaitFor(Duration(10, TimeUnit.SECONDS))
    }
  }
}
