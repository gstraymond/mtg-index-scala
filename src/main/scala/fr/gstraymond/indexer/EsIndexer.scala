package fr.gstraymond.indexer

import java.io.File
import java.text.Normalizer

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard
import fr.gstraymond.utils.Log
import play.api.libs.json.Json

import scala.concurrent.Future

object EsIndexer extends Log {

  val host = "localhost:9200"
  val index = "magic"
  val `type` = "card"
  val indexPath = s"http://$host/$index"
  val bulkPath = s"http://$host/_bulk"

  val bulk = 200

  def delete(): Future[Unit] = {
    Http {
      url(indexPath).DELETE OK as.String
    }.map { result =>
      log.info(s"delete: $result")
      ()
    }
  }

  def configure(): Future[Unit] = {
    val file = new File(getClass.getResource("/indexer/config.json").getFile)
    Http {
      url(indexPath).PUT <<< file OK as.String
    }.map { result =>
      log.info(s"configure: $result")
    }
  }

  def index(cards: Seq[MTGCard]): Future[Unit] = {
    Future.sequence {
      cards.grouped(bulk).map { bulk =>
        val body = bulk.flatMap { card =>
          val indexJson = Json.obj("index" -> Json.obj(
            "_index" -> index,
            "_type" -> `type`,
            "_id" -> norm(card.title)
          ))

          import fr.gstraymond.model.MTGCardFormat._
          val cardJson = Json.toJson(card)

          Seq(indexJson, cardJson).map(Json.stringify)
        }.mkString("\n")

        Http {
          url(bulkPath).POST << body OK as.String
        }.map { response =>
          log.info(s"bulk done...")
        }
      }.toSeq
    }.map { _ =>
      log.info(s"bulk finished !")
    }
  }

  private def norm(string: String) = {
    Normalizer.normalize(string, Normalizer.Form.NFKD)
      .replace(" ", "-")
      .replaceAll("[^-\\w]", "")
      .toLowerCase()
  }
}
