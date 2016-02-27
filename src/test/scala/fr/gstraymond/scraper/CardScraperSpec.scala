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
class CardScraperSpec(implicit ee: ExecutionEnv) extends Specification {

  "card scraper" should {
    "scrap" in {
      val editions = Seq(
        ScrapedEdition("lg", "", None, None),
        ScrapedEdition("mi", "", None, None)
      )

      CardScraper.scrap(editions, Seq("en", "fr")) must be_==(Map.empty).awaitFor(Duration(10, TimeUnit.SECONDS))
    }
  }
}
