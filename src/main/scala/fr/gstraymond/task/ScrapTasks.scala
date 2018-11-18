package fr.gstraymond.task

import fr.gstraymond.dl.{CardPictureDownloader, EditionPictureDownloader}
import fr.gstraymond.indexer.{EsAutocompleteIndexer, EsCardIndexer, EsRulesIndexer}
import fr.gstraymond.model._
import fr.gstraymond.parser.AllSetConverter
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.rules.parser.RulesParser
import fr.gstraymond.scraper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PriceScrapTask extends Task[Seq[ScrapedPrice]] {
  override def process: Future[Seq[ScrapedPrice]] = {
    PriceScraper.scrap.map(storePrices)
  }
}

object PriceProcessTask extends Task[Seq[ScrapedCard]] {
  override def process: Future[Seq[ScrapedCard]] = {
    Future.successful {
      PriceScraper.process(loadScrapedCards, loadPrices)
    }
  }
}

object FormatScrapTask extends Task[Seq[ScrapedFormat]] {
  override def process: Future[Seq[ScrapedFormat]] = FormatScraper.scrap.map(storeFormats)
}

object CardPictureDLTask extends Task[Unit] {
  override def process: Future[Unit] = CardPictureDownloader.download(loadMTGCards)
}

object EditionPictureDLTask extends Task[Unit] {
  override def process: Future[Unit] = EditionPictureDownloader.download(loadMTGCards)
}

object RulesScrapTask extends Task[Rules] {
  override def process: Future[Rules] = RulesScraper.scrap.map((RulesParser.parse _).tupled).map { rules =>
    storeRules(rules)
    rules
  }
}

object AllSetScrapTask extends Task[Unit] {
  override def process: Future[Unit] = AllSetScraper.scrap
}

object AllSetConvertTask extends Task[Seq[MTGCard]] {
  override def process: Future[Seq[MTGCard]] = {
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
  override def process: Future[Seq[MTGCard]] = {
    for {
      _ <- AllSetScraper.scrap
      rawRules <- RulesScraper.scrap
      rules = (RulesParser.parse _).tupled(rawRules)
      abilities <- AbilityScraper.scrap
      formats <- FormatScraper.scrap
      prices <- PriceScraper.scrap
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