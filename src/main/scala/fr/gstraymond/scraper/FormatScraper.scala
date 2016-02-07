package fr.gstraymond.scraper

import fr.gstraymond.model.ScrapedFormat
import fr.gstraymond.scraper.format._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FormatScraper extends MTGSalvationScraper {

  val scraps = Seq(
    StandardFormatScrap,
    ExtendedFormatScrap,
    ModernFormatScrap,
    LegacyFormatScrap,
    VintageFormatScrap,
    VintageRestrictedFormatScrap
  )

  def scrap: Future[Seq[ScrapedFormat]] = {
    Future.sequence {
      scraps.map { scrap =>
        curl(scrap.path).map { doc =>
          ScrapedFormat(
            scrap.name,
            scrap.currentRotation(doc),
            scrap.bannedCards(doc),
            scrap.restrictedCards(doc)
          )
        }
      }
    }
  }
}
