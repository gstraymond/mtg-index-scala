package fr.gstraymond.task

import fr.gstraymond.importer.OracleImporter
import fr.gstraymond.model.{MTGCard, RawCard}
import fr.gstraymond.parser.CardConverter

import scala.concurrent.Future

object ImportTask extends Task[Seq[RawCard]] {

  override def process = Future.successful {
    val rawCards = OracleImporter.`import`("/All-Sets-2015-11-15.txt")
    storeRawCards(rawCards)
  }
}

object CardConvertTask extends Task[Seq[MTGCard]] {

  override def process = Future.successful {
    CardConverter.convert(loadRawCards, loadScrapedCards)
  }
}