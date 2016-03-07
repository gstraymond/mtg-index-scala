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
  val bulkPath = s"$indexPath/${`type`}/_bulk"

  val bulk = 500

  def delete(): Future[Unit] = {
    Http {
      url(indexPath).DELETE > as.String
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
    val grouped = cards.grouped(bulk).toStream
    val groupedSize = grouped.size
    val cardSize = cards.size

    grouped
      .zipWithIndex
      .foldLeft(Future.successful(0)) { case (acc, (group, i)) =>
        for {
          count <- acc
          _ <- {
            Http {
              url(bulkPath).POST << buildBody(group) OK as.String
            }.map { _ =>
              log.info(s"prcessed: ${i + 1}/$groupedSize bulks - ${count + group.size}/$cardSize cards")
            }
          }
        } yield {
          count + group.size
        }
      }
      .map { _ => log.info(s"bulk finished !") }
  }

  private def buildBody(group: Seq[MTGCard]): String = {
    group.flatMap { card =>

      val indexJson = Json.obj("index" -> Json.obj("_id" -> norm(card.title)))

      import fr.gstraymond.model.MTGCardFormat._
      val cardJson = Json.toJson(card)

      Seq(indexJson, cardJson).map(Json.stringify)
    }.mkString("\n") + "\n"
  }

  private def norm(string: String) = StringUtils.normalize(string)
}
