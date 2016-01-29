package fr.gstraymond.scraper

import org.junit.Ignore
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@Ignore
@RunWith(classOf[JUnitRunner])
class EditionScraperSpec(implicit ee: ExecutionEnv) extends Specification {

  "edition scraper" should {
    "scrap" in {
      EditionScraper.scrap must be_!=(Map.empty).await
    }
  }
}
