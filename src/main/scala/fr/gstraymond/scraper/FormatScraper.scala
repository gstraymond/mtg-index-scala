package fr.gstraymond.scraper

import fr.gstraymond.model.ScrapedFormat
import fr.gstraymond.scraper.format.*
import fr.gstraymond.utils.Log

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FormatScraper extends MTGSalvationScraper with Log {

  val scrapers: Seq[FormatScrap] = Seq(
    // StandardFormatScrap,
    // ExtendedFormatScrap,
    // ModernFormatScrap,
    // LegacyFormatScrap,
    // CommanderFormatScrap,
    // PauperFormatScrap,
    // VintageFormatScrap,
    // VintageRestrictedFormatScrap
  )

  def scrap: Future[Seq[ScrapedFormat]] =
    Future.traverse(scrapers) { scraper =>
      scrap(scraper.path, followRedirect = true).map { doc =>
        val format = ScrapedFormat(
          scraper.name,
          scraper.currentRotation(doc).toSet,
          scraper.bannedCards(doc).toSet,
          scraper.restrictedCards(doc).toSet
        )
        log.info(s"format scraped: $format")
        format
      }
    }
}
