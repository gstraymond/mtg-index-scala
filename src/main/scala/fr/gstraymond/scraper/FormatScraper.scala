package fr.gstraymond.scraper

import fr.gstraymond.model.ScrapedFormat
import fr.gstraymond.scraper.format._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FormatScraper extends MTGSalvationScraper {

  val scrapers = Seq(
    StandardFormatScrap,
    ExtendedFormatScrap,
    ModernFormatScrap,
    LegacyFormatScrap,
    VintageFormatScrap,
    VintageRestrictedFormatScrap
  )

  def scrap: Future[Seq[ScrapedFormat]] = {
    Future.sequence {
      scrapers.map { scraper =>
        scrap(scraper.path, followRedirect = true).map { doc =>
          ScrapedFormat(
            scraper.name,
            scraper.currentRotation(doc),
            scraper.bannedCards(doc),
            scraper.restrictedCards(doc)
          )
        }
      }
    }
  }
}
