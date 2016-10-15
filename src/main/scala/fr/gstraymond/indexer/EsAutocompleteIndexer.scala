package fr.gstraymond.indexer

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard
import play.api.libs.json.Json

import scala.concurrent.Future

object EsAutocompleteIndexer extends EsIndexer {

  override val index = "autocomplete"
  override val `type` = "card"

  override def index(cards: Seq[MTGCard]): Future[Unit] = for {
    _ <- super.index(cards)
    _ <- index("token", extractTokens(cards))
    _ <- index("edition", extractEditions(cards))
    _ <- index("special", extractSpecials(cards))
  } yield ()

  override def buildBody(group: Seq[MTGCard]) = {
    group.flatMap { card =>
      val indexJson = Json.obj("index" -> Json.obj("_id" -> getId(card)))
      val cardJson = Json.obj("suggest" -> card.title)
      Seq(indexJson, cardJson).map(Json.stringify)
    }.mkString("\n") + "\n"
  }

  private def extractTokens(cards: Seq[MTGCard]): Map[String, Int] = {
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
  }

  private def extractEditions(cards: Seq[MTGCard]): Map[String, Int] = {
    (for {
      card <- cards
      pub <- card.publications
    } yield {
      pub.edition
    }).distinct.map(_ -> 2).toMap
  }

  private def extractSpecials(cards: Seq[MTGCard]): Map[String, Int] = {
    cards.flatMap(_.special).groupBy(_.toLowerCase).mapValues(_.size)
  }

  private def index(`type`: String, data: Map[String, Int]): Future[Unit] = {
    val body = data
      .flatMap { case (token, weight) =>
        val indexJson = Json.obj("index" -> Json.obj("_id" -> s"${`type`}-$token-$weight"))
        val cardJson = Json.obj("suggest" -> Json.obj("input" -> token, "weight" -> weight))
        Seq(indexJson, cardJson)
      }.mkString("\n") + "\n"

    Http {
      url(bulkPath).POST << body OK as.String
    }.map { _ =>
      log.info(s"processed: ${data.size} ${`type`}")
    }
  }
}
