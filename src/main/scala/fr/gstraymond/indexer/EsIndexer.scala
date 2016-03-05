package fr.gstraymond.indexer

import java.io.File

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard
import fr.gstraymond.utils.{Log, StringUtils}
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

  def exists(cards: Seq[MTGCard]): Future[Seq[String]] = Future.sequence {
    cards.map { card =>
      val s = s"$indexPath/${`type`}/${norm(card.title)}-${norm(card.`type`)}"
      //log.info(s"uri: $s")
      Http {
        url(s) > as.String
      }.map { resp =>
        (Json.parse(resp) \ "found").as[Boolean] match {
          case true => None
          case _ =>
            log.info(s"${card.title}")
            Some(card.title)
        }
      }
    }
  }.map(_.flatten).map { r =>
    log.info(s"result: $r")
    r
  }

  def index(cards: Seq[MTGCard]): Future[Unit] = {
    Future.sequence {
      var count = 0
      val grouped = cards.grouped(bulk).toSeq
      val groupedSize = grouped.size
      val cardSize = cards.size
      grouped.zipWithIndex.map { case (group, i) =>
        val body = group.flatMap { card =>

          val indexJson = Json.obj("index" -> Json.obj(
            "_index" -> index,
            "_type" -> `type`,
            "_id" -> norm(card.title)
          ))

          import fr.gstraymond.model.MTGCardFormat._
          val cardJson = Json.toJson(card)

          Seq(indexJson, cardJson).map(Json.stringify)
        }.mkString("\n") + "\n"

        Http {
          url(bulkPath).POST << body OK as.String
        }.map { response =>
          count = count + group.size
          log.info(s"bulk done... ${i + 1}/$groupedSize - $count/$cardSize")
        }
      }
    }.map { _ =>
      log.info(s"bulk finished !")
    }
  }

  private def norm(string: String) = StringUtils.normalize(string)
}
