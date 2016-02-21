package fr.gstraymond.task

import fr.gstraymond.dl.CardPictureDownloader
import fr.gstraymond.model.{ScrapedEdition, ScrapedCard, ScrapedFormat, ScrapedPrice}
import fr.gstraymond.scraper._
import fr.gstraymond.utils.FileUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FullScrapTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    for {
      editions <- EditionScraper.scrap
      editionsWithDate <- ReleaseDateScraper.scrap(editions)
      cards <- CardScraper.scrap(editionsWithDate, FileUtils.langs)
      prices <- PriceScraper.scrap
      cardsWithPrice <- Future.successful(PriceScraper.process(cards, prices))
      formats <- FormatScraper.scrap
    } yield {
      storeFormats(formats)
      storeEditions(editionsWithDate)
      storePrices(prices)
      storeScrapedCards(cardsWithPrice)
    }
  }
}

object CardScrapTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    for {
      editions <- EditionScraper.scrap
      editionsWithDate <- ReleaseDateScraper.scrap(editions)
      cards <- CardScraper.scrap(editionsWithDate, FileUtils.langs)
    } yield {
      storeEditions(editionsWithDate)
      storeScrapedCards(cards)
    }
  }
}

object PriceScrapTask extends Task[Seq[ScrapedPrice]] {
  override def process = {
    PriceScraper.scrap.map(storePrices)
  }
}

object PriceProcessTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    Future.successful {
      PriceScraper.process(loadScrapedCards, loadPrices)
    }
  }
}

object FormatScrapTask extends Task[Seq[ScrapedFormat]] {
  override def process = FormatScraper.scrap.map(storeFormats)
}

object ReleaseDateScrapTask extends Task[Seq[ScrapedEdition]] {
  override def process = ReleaseDateScraper.scrap(loadEditions)
}

object CardictureDLTask extends Task[Unit] {
  override def process = CardPictureDownloader.download(loadMTGCards)
}

object OracleScrapTask extends Task[Unit] {
  override def process = OracleScraper.scrap()
}