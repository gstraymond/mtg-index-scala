package fr.gstraymond.task

import fr.gstraymond.model.{MTGCard, RawCard}
import fr.gstraymond.parser.{CardConverter, OracleRawCardConverter, OracleRawParser}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object OracleConvertTask extends Task[Seq[RawCard]] {

  override def process = {
    Future.successful {
      loadOracle
    }.map {
      OracleRawParser.parse
    }.map {
      OracleRawCardConverter.convert
    }.map {
      storeRawCards
    }
  }
}

object CardConvertTask extends Task[Seq[MTGCard]] {

  override def process = Future.successful {
    CardConverter.convert(loadRawCards, loadScrapedCards, loadFormats)
  }.map {
    storeMTGCards
  }
}