package fr.gstraymond.task

import fr.gstraymond.dl.{CardPictureDownloader, EditionPictureDownloader}
import fr.gstraymond.indexer.{EsAutocompleteIndexer, EsCardIndexer, EsRulesIndexer}
import fr.gstraymond.model._
import fr.gstraymond.parser.{AllSetConverter, CardConverter, OracleConverter}
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.rules.parser.RulesParser
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

object RulesScrapTask extends Task[Rules] {
  override def process = RulesScraper.scrap.map((RulesParser.parse _).tupled).map { rules =>
    storeRules(rules)
    rules
  }
}

@deprecated("use DEALTask", "")
object DoZeMagicTask extends Task[Seq[MTGCard]] {
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
      _ <- EsCardIndexer.delete()
      _ <- EsCardIndexer.configure()
      _ <- EsCardIndexer.index(mtgCards)
      _ <- EsAutocompleteIndexer.delete()
      _ <- EsAutocompleteIndexer.configure()
      _ <- EsAutocompleteIndexer.index(mtgCards)
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

object AllSetScrapTask extends Task[Unit] {
  override def process = AllSetScraper.scrap
}

object AllSetConvertTask extends Task[Seq[MTGCard]] {
  override def process = {
    for {
      abilities <- AbilityScraper.scrap
      formats <- FormatScraper.scrap
      mtgCards <- AllSetConverter.convert(loadAllSet, formats, loadPrices, abilities)
      _ <- EditionPictureDownloader.download(mtgCards)
      _ <- CardPictureDownloader.download(mtgCards)
      _ <- EsCardIndexer.delete()
      _ <- EsCardIndexer.configure()
      _ <- EsCardIndexer.index(mtgCards)
      _ <- EsAutocompleteIndexer.delete()
      _ <- EsAutocompleteIndexer.configure()
      _ <- EsAutocompleteIndexer.index(mtgCards)
    } yield {
      storeMTGCards(mtgCards)
    }
  }
}

object DEALTask extends Task[Seq[MTGCard]] {
  override def process = {
    for {
      _ <- AllSetScraper.scrap
      abilities <- AbilityScraper.scrap
      formats <- FormatScraper.scrap
      prices <- PriceScraper.scrap
      rawRules <- RulesScraper.scrap
      rules = (RulesParser.parse _).tupled(rawRules)
      mtgCards <- AllSetConverter.convert(loadAllSet, formats, prices, abilities)
      _ <- EditionPictureDownloader.download(mtgCards)
      _ <- CardPictureDownloader.download(mtgCards)
      _ <- EsCardIndexer.delete()
      _ <- EsCardIndexer.configure()
      _ <- EsCardIndexer.index(mtgCards)
      _ <- EsAutocompleteIndexer.delete()
      _ <- EsAutocompleteIndexer.configure()
      _ <- EsAutocompleteIndexer.index(mtgCards)
      _ <- EsRulesIndexer.delete()
      _ <- EsRulesIndexer.configure()
      _ <- EsRulesIndexer.index(Seq(rules))
    } yield {
      storeRules(rules)
      storeFormats(formats)
      storePrices(prices)
      storeMTGCards(mtgCards)
    }
  }
}