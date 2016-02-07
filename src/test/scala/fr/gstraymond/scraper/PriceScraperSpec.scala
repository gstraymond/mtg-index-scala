package fr.gstraymond.scraper

import java.util.concurrent.TimeUnit

import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json

import scala.concurrent.duration.Duration

//@Ignore
@RunWith(classOf[JUnitRunner])
class PriceScraperSpec(implicit ee: ExecutionEnv) extends Specification {

  "price scraper" should {
    "scrap" in {
      /*
      val editions = Seq(scrapEditionUrls
        ScrapedEdition("oath", "Oath of the Gatewatch",
          Some(ScrapedReleaseDate(new Date(), "/spoiler_lists/Oath_of_the_Gatewatch"))),
        ScrapedEdition("zendi", "Battle for Zendikar",
          Some(ScrapedReleaseDate(new Date(), "/spoiler_lists/Battle_for_Zendikar")))
      )

      val cards = Seq(
        ScrapedCard("", "", "", "oath", "World Breaker", None, Seq.empty),
        ScrapedCard("", "", "", "zendi", "Oblivion Sower", None, Seq.empty)
      )

      PriceScraper.scrap(editions, cards) must be_==("").awaitFor(Duration(10, TimeUnit.SECONDS))
      */

      PriceScraper.scrapEditionUrls().map(Json.toJson(_)) must be_==(Map.empty).awaitFor(Duration(10, TimeUnit.SECONDS))
    }
    "scrap 2" in {
      PriceScraper.scrapEditionPrices("/index/7E") must be_==("").awaitFor(Duration(10, TimeUnit.SECONDS))
    }
  }
}
