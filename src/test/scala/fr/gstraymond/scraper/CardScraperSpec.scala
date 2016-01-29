package fr.gstraymond.scraper

import fr.gstraymond.model.ScrapedEdition
import org.junit.Ignore
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@Ignore
@RunWith(classOf[JUnitRunner])
class CardScraperSpec(implicit ee: ExecutionEnv) extends Specification {

  "card scraper" should {
    "scrap" in {
      val editions = Seq(ScrapedEdition("lg", ""), ScrapedEdition("mi", ""))
      CardScraper.scrap(editions, Seq("en", "fr")) must be_==(Map.empty).await
    }
  }
}
