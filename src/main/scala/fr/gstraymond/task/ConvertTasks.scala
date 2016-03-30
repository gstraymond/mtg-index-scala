package fr.gstraymond.task

import fr.gstraymond.model.{MTGSetCard, MTGCard, RawCard}
import fr.gstraymond.parser._

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

object SetCardConvertTask extends Task[Map[String, Seq[MTGSetCard]]] {

  override def process = Future.successful {
    MTGSetCardConverter.convert(loadMTGCards)
  }.map {
    storeMTGSetCards
  }
}