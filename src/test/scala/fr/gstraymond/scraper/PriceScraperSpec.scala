package fr.gstraymond.scraper

import java.util.concurrent.TimeUnit

import org.junit.Ignore
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json

import scala.concurrent.duration.Duration

@Ignore
@RunWith(classOf[JUnitRunner])
class PriceScraperSpec(implicit ee: ExecutionEnv) extends Specification {

  "price scraper" should {
    "scrap" in {
      PriceScraper.scrapEditionUrls.map(Json.toJson(_)) must be_==(Map.empty).awaitFor(Duration(10, TimeUnit.SECONDS))
    }
    "scrap 2" in {
      PriceScraper.scrapEditionPrices("/index/7E") must be_==("").awaitFor(Duration(10, TimeUnit.SECONDS))
    }
  }
}
