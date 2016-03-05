package fr.gstraymond.task

import fr.gstraymond.dl.{EditionPictureDownloader, CardPictureDownloader}
import fr.gstraymond.indexer.EsIndexer
import fr.gstraymond.model.{ScrapedEdition, ScrapedCard, ScrapedFormat, ScrapedPrice}
import fr.gstraymond.parser.{CardConverter, OracleConverter}
import fr.gstraymond.scraper._
import fr.gstraymond.utils.FileUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FullScrapTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    for {
      editions <- EditionScraper.scrap
      editionsWithDate <- ReleaseDateScraper.scrap(editions)
      (editionsWithStdCode, cache) <- GathererEditionCodeScraper.scrap(editionsWithDate, loadStdCodeCache)
      cards <- CardScraper.scrap(editionsWithStdCode, FileUtils.langs)
      prices <- PriceScraper.scrap
      cardsWithPrice = PriceScraper.process(cards, prices)
      formats <- FormatScraper.scrap
    } yield {
      storeFormats(formats)
      storeEditions(editionsWithStdCode)
      storeStdCodeCache(cache)
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

object CardPictureDLTask extends Task[Unit] {
  override def process = CardPictureDownloader.download(loadMTGCards)
}

object EditionPictureDLTask extends Task[Unit] {
  override def process = EditionPictureDownloader.download(loadMTGCards)
}

object OracleScrapTask extends Task[Unit] {
  override def process = OracleScraper.scrap
}

object GathererEditionCodeScrapTask extends Task[Seq[ScrapedEdition]] {
  override def process = GathererEditionCodeScraper.scrap(loadEditions, loadStdCodeCache).map { case (editions, cache) =>
    storeStdCodeCache(cache)
    editions
  }
}

object DoZeMagicTask extends Task[Seq[ScrapedCard]] {
  override def process = {
    for {
      // editions
      _editions <- EditionScraper.scrap
      __editions <- ReleaseDateScraper.scrap(_editions)
      (editions, cache) <- GathererEditionCodeScraper.scrap(__editions, loadStdCodeCache)
      // scraped cards with prices
      _scrapedCards <- CardScraper.scrap(editions, FileUtils.langs)
      prices <- PriceScraper.scrap
      scrapedCards = PriceScraper.process(_scrapedCards, prices)
      // formats
      formats <- FormatScraper.scrap
      // oracle
      _ <- OracleScraper.scrap
      rawCards = OracleConverter.convert(loadOracle)
      // mtg cards
      mtgCards = CardConverter.convert(rawCards, scrapedCards, formats)
      _ <- EditionPictureDownloader.download(mtgCards)
      _ <- CardPictureDownloader.download(mtgCards)
      _ <- EsIndexer.delete()
      _ <- EsIndexer.configure()
      _ <- EsIndexer.index(mtgCards)
    } yield {
      storeRawCards(rawCards)
      storeFormats(formats)
      storeEditions(editions)
      storeStdCodeCache(cache)
      storePrices(prices)
      storeScrapedCards(scrapedCards)
      storeMTGCards(mtgCards)
    }
  }
}