package fr.gstraymond.scraper

import fr.gstraymond.model.{ScrapedEdition, ScrapedCard}
import fr.gstraymond.utils.Log
import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CardScraper extends MagicCardsInfoScraper with Log {

  val cardExpression = "table[cellpadding=3] tr"

  def scrap(editions: Seq[ScrapedEdition], langs: Seq[String]): Future[Seq[ScrapedCard]] = {

    val eventualDocuments = for {
      edition <- editions
      language <- langs
    } yield {
      get(s"/${edition.code}/$language.html").map {
        (edition, language, _)
      }
    }

    Future.sequence(eventualDocuments).map { tuples =>
      tuples.flatMap {
        case (edition, language, doc) =>
          (doc.select(cardExpression).asScala match {
            case head +: tail => tail
            case _ =>
              log.info(s"No results found for : url '${doc.location()}' and expression '$cardExpression'")
              Seq.empty
          }).map { elem =>
            buildScrapedCard(elem, edition, language)
          }
      }
    }.map {
      mergeCards
    }
  }

  private def buildScrapedCard(element: Element, edition: ScrapedEdition, language: String) = {
    val tds = element.getElementsByTag("td").asScala
    tds match {
      case Seq(collectorNumber, title, _, _, rarity, artist, _) =>
        ScrapedCard(
          collectorNumber = collectorNumber.text(),
          rarity = rarity.text(),
          artist = artist.text(),
          edition = edition,
          title =  if (language == "en") title.text() else "",
          frenchTitle = if (language == "fr") Some(title.text()) else None
        )
      case _ => throw new RuntimeException(s"tds $tds not matched")
    }
  }

  private def mergeCards(scrapedCards: Seq[ScrapedCard]): Seq[ScrapedCard] = {
    scrapedCards.groupBy(_.uniqueId).map { case (_, cards) =>
      cards.sortBy(_.title) match {
        case Seq(frCard, enCard) => enCard.copy(frenchTitle = frCard.frenchTitle)
        case Seq(enCard) => enCard
      }
    }.toSeq
  }
}
