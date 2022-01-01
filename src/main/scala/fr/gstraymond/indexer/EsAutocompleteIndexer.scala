package fr.gstraymond.indexer

import com.github.plokhotnyuk.jsoniter_scala.core._
import dispatch.Defaults._
import dispatch._
import fr.gstraymond.indexer.IndexerModels._
import fr.gstraymond.model.MTGCard

import java.nio.charset.Charset
import scala.concurrent.Future

object EsAutocompleteIndexer extends EsIndexer[MTGCard] {

  override val index = "autocomplete"

  override def index(cards: Seq[MTGCard]): Future[Unit] = for
    _ <- super.index(cards)
    _ <- indexSuggest("token", extractTokens(cards))
    _ <- indexSuggest("edition", extractEditions(cards))
    _ <- indexSuggest("special", extractSpecials(cards))
  yield ()

  override def buildBody(group: Seq[MTGCard]): String =
    group
      .flatMap { card =>
        val indexJson = Index(IndexId(getId(card)))
        val cardJson = Autocomplete(
          Suggest(card.title),
          colors = Some(card.colors),
          `type` = Some(card.`type`),
          land = Some(card.land)
        )
        Seq(writeToString(indexJson), writeToString(cardJson))
      }
      .mkString("\n") + "\n"

  private def extractTokens(cards: Seq[MTGCard]): Seq[Autocomplete] = {
    val tokenOccurrences =
      cards
        .flatMap(c => c.description.toLowerCase.split(" ") ++ c.`type`.toLowerCase.split(" "))
        .flatMap(_.split("\n"))
        .flatMap(_.split("-"))
        .filterNot(_.contains("{"))
        .filterNot(_.exists(_.isDigit))
        .filterNot(_.endsWith("'t"))
        .filterNot(_.contains("/"))
        .map {
          _.replace(",", "")
            .replace(".", "")
            .replace(":", "")
            .replace("(", "")
            .replace(")", "")
            .replace("\"", "")
            .replace("'s", "")
            .replace("'", "")
            .replace(";", "")
        }
        .filter(_.length > 3)
        .groupBy(a => a)
        .view
        .mapValues(_.size)

    tokenOccurrences.toSeq
      .foldLeft(Seq[(String, Int)]()) { case (acc, (k, v)) =>
        acc ++ {
          if k.endsWith("s") && tokenOccurrences.contains(k.dropRight(1)) then Seq(k.dropRight(1) -> v)
          else Seq(k                                                                              -> v)
        }
      }
      .groupBy(_._1)
      .view
      .mapValues(_.map(_._2).sum)
      .filter(_._2 > 5)
      .toSeq
      .map { case (input, weight) => Autocomplete(Suggest(input, Some(weight))) }
  }

  private def extractEditions(cards: Seq[MTGCard]): Seq[Autocomplete] = {
    (for
      card <- cards
      pub  <- card.publications
    yield {
      pub.edition -> pub.stdEditionCode
    }).distinct
      .map {
        case (edition, Some(stdCode)) => Autocomplete(Suggest(edition, Some(2)), stdEditionCode = Some(stdCode))
        case (edition, _)             => Autocomplete(Suggest(edition, Some(2)))
      }
  }

  private def extractSpecials(cards: Seq[MTGCard]): Seq[Autocomplete] = {
    cards.flatMap(_.special).groupBy(_.toLowerCase).view.mapValues(_.size).toSeq.map { case (input, weight) =>
      Autocomplete(Suggest(input, Some(weight)))
    }
  }

  private def indexSuggest(`type`: String, autocompletes: Seq[Autocomplete]): Future[Unit] = {
    val body = autocompletes
      .flatMap { autocomplete =>
        val indexJson =
          Index(IndexId(s"${`type`}-${autocomplete.suggest.input}-${autocomplete.suggest.weight.getOrElse(0)}"))
        val cardJson = autocomplete
        Seq(writeToString(indexJson), writeToString(cardJson))
      }
      .mkString("\n") + "\n"

    Http
      .default {
        url(bulkPath).POST
          .setContentType(
            "application/json",
            Charset.forName("utf-8")
          ) << body OK as.String
      }
      .map { _ =>
        log.info(s"processed: ${autocompletes.size} ${`type`}")
      }
  }
}
