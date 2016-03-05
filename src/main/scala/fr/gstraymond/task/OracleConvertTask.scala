package fr.gstraymond.task

import fr.gstraymond.model.{MTGCard, RawCard}
import fr.gstraymond.parser.{OracleConverter, CardConverter, OracleRawCardConverter, OracleRawParser}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object OracleConvertTask extends Task[Seq[RawCard]] {

  override def process = {
    Future.successful {
      loadOracle
    }.map {
      OracleConverter.convert
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