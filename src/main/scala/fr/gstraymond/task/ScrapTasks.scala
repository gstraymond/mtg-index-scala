package fr.gstraymond.task

import fr.gstraymond.dl.CardPictureDownloader
import fr.gstraymond.dl.EditionPictureDownloader
import fr.gstraymond.indexer.EsAutocompleteIndexer
import fr.gstraymond.indexer.EsCardIndexer
import fr.gstraymond.indexer.EsRulesIndexer
import fr.gstraymond.model._
import fr.gstraymond.parser.AllSetConverter
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.rules.parser.RulesParser
import fr.gstraymond.scraper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FormatScrapTask extends Task[Seq[ScrapedFormat]]:
  override def process: Future[Seq[ScrapedFormat]] = FormatScraper.scrap.map(storeFormats)

object CardPictureDLTask extends Task[Unit]:
  override def process: Future[Unit] = CardPictureDownloader.download(loadMTGCards)

object EditionPictureDLTask extends Task[Unit]:
  override def process: Future[Unit] = EditionPictureDownloader.download(loadMTGCards)

object RulesScrapTask extends Task[Rules]:
  override def process: Future[Rules] = RulesScraper.scrap.map((RulesParser.parse _).tupled).map { rules =>
    storeRules(rules)
    rules
  }

object AllSetScrapTask extends Task[Unit]:
  override def process: Future[Unit] = AllSetScraper.scrap

object AllPricesScrapTask extends Task[Unit]:
  override def process: Future[Unit] =
    for
      prices <- AllPricesScraper.scrap
      _ = storePrices(prices)
    yield ()

object AllSetConvertTask extends Task[Seq[MTGCard]]:
  override def process: Future[Seq[MTGCard]] =
    for
      abilities <- AbilityScraper.scrap
      mtgCards  <- AllSetConverter.convert(loadAllSet, abilities, loadAllPrices)
      _         <- EditionPictureDownloader.download(mtgCards)
      _         <- CardPictureDownloader.download(mtgCards)
      _         <- EsCardIndexer.delete()
      _         <- EsCardIndexer.configure()
      _         <- EsCardIndexer.index(mtgCards)
      _         <- EsAutocompleteIndexer.delete()
      _         <- EsAutocompleteIndexer.configure()
      _         <- EsAutocompleteIndexer.index(mtgCards)
      rawRules  <- RulesScraper.scrap
      rules = (RulesParser.parse _).tupled(rawRules)
      _ <- EsRulesIndexer.delete()
      _ <- EsRulesIndexer.configure()
      _ <- EsRulesIndexer.index(Seq(rules))
    yield storeMTGCards(mtgCards)

object MtgIndexScala extends Task[Unit]:
  override def process: Future[Unit] =
    for
      allPrices <- AllPricesScraper.scrap
      _         <- AllSetScraper.scrap
      abilities <- AbilityScraper.scrap
      mtgCards  <- AllSetConverter.convert(loadAllSet, abilities, allPrices)
      _         <- EditionPictureDownloader.download(mtgCards)
      _         <- CardPictureDownloader.download(mtgCards)
      _         <- EsCardIndexer.delete()
      _         <- EsCardIndexer.configure()
      _         <- EsCardIndexer.index(mtgCards)
      _         <- EsAutocompleteIndexer.delete()
      _         <- EsAutocompleteIndexer.configure()
      _         <- EsAutocompleteIndexer.index(mtgCards)
      rawRules  <- RulesScraper.scrap
      rules = (RulesParser.parse _).tupled(rawRules)
      _ <- EsRulesIndexer.delete()
      _ <- EsRulesIndexer.configure()
      _ <- EsRulesIndexer.index(Seq(rules))
    yield ()
