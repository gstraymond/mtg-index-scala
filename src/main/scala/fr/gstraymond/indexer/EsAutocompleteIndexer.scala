package fr.gstraymond.indexer

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard

import scala.concurrent.Future

object EsAutocompleteIndexer extends EsIndexer[MTGCard] {

  override val index = "autocomplete"
  override val `type` = "card"

  override def index(cards: Seq[MTGCard]): Future[Unit] = for {
    _ <- super.index(cards)
    _ <- indexSuggest("token", extractTokens(cards))
    _ <- indexSuggest("edition", extractEditions(cards))
    _ <- indexSuggest("special", extractSpecials(cards))
  } yield ()

  override def buildBody(group: Seq[MTGCard]): String = {
    group.flatMap { card =>
      val payload = Payload(colors = Some(card.colors), `type` = Some(card.`type`), land = Some(card.land))
      val indexJson = Index(IndexId(getId(card)))
      val cardJson = Autocomplete(Suggest(card.title, None, Some(payload)))
      Seq(writeToString(indexJson), writeToString(cardJson))
    }.mkString("\n") + "\n"
  }

  case class Autocomplete(suggest: Suggest)

  case class Suggest(input: String, weight: Option[Int], payload: Option[Payload])

  case class Payload(colors: Option[Seq[String]] = None,
                     `type`: Option[String] = None,
                     land: Option[Seq[String]] = None,
                     stdEditionCode: Option[String] = None)

  implicit val AutocompleteCodec: JsonValueCodec[Autocomplete] = JsonCodecMaker.make[Autocomplete](CodecMakerConfig())

  private def extractTokens(cards: Seq[MTGCard]): Seq[Suggest] = {
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
          _
            .replace(",", "")
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
        .mapValues(_.size)

    tokenOccurrences
      .toSeq
      .foldLeft(Seq[(String, Int)]()) { case (acc, (k, v)) =>
        acc ++ {
          if (k.endsWith("s") && tokenOccurrences.contains(k.dropRight(1))) Seq(k.dropRight(1) -> v)
          else Seq(k -> v)
        }
      }
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)
      .filter(_._2 > 5)
      .map { case (input, weight) => Suggest(input, Some(weight), None)}
      .toSeq
  }

  private def extractEditions(cards: Seq[MTGCard]): Seq[Suggest] = {
    (for {
      card <- cards
      pub <- card.publications
    } yield {
      pub.edition -> pub.stdEditionCode
    })
      .distinct
      .map {
        case (edition, Some(stdCode)) => Suggest(edition, Some(2), Some(Payload(stdEditionCode = Some(stdCode))))
        case (edition, _) => Suggest(edition, Some(2), None)
      }
  }

  private def extractSpecials(cards: Seq[MTGCard]): Seq[Suggest] = {
    cards.flatMap(_.special).groupBy(_.toLowerCase).mapValues(_.size).map { case (input, weight) =>
        Suggest(input, Some(weight), None)
    }.toSeq
  }

  private def indexSuggest(`type`: String, suggests: Seq[Suggest]): Future[Unit] = {
    val body = suggests.flatMap { suggest =>
      val indexJson = Index(IndexId(s"${`type`}-${suggest.input}-${suggest.weight.getOrElse(0)}"))
      val cardJson = Autocomplete(suggest)
      Seq(writeToString(indexJson), writeToString(cardJson))
    }.mkString("\n") + "\n"

    Http.default {
      url(bulkPath).POST << body OK as.String
    }.map { _ =>
      log.info(s"processed: ${suggests.size} ${`type`}")
    }
  }
}
