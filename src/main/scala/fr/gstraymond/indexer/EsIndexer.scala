package fr.gstraymond.indexer

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard
import fr.gstraymond.utils.{Log, StringUtils}

import scala.concurrent.Future
import scala.io.Source

trait EsIndexer[A] extends Log {

  val host = "localhost:9200"

  def index: String
  def `type`: String

  def indexPath = s"http://$host/$index"

  def bulkPath = s"$indexPath/${`type`}/_bulk"

  val bulk = 500

  def delete(): Future[Unit] = {
    Http.default {
      url(indexPath).DELETE > as.String
    }.map { result =>
      log.info(s"delete: $result")
      ()
    }
  }

  def configure(): Future[Unit] = {
    val body = Source.fromInputStream(getClass.getResourceAsStream(s"/indexer/$index.config.json")).mkString
    Http.default {
      url(indexPath).PUT << body OK as.String
    }.map { result =>
      log.info(s"configure: $result")
    }
  }

  def index(elems: Seq[A]): Future[Unit] = {
    val grouped = elems.grouped(bulk).toStream
    val groupedSize = grouped.size
    val cardSize = elems.size

    grouped
      .zipWithIndex
      .foldLeft(Future.successful(0)) { case (acc, (group, i)) =>
        for {
          count <- acc
          _ <- {
            Http.default {
              url(bulkPath).POST << buildBody(group) OK as.String
            }.map { _ =>
              log.info(s"processed: ${i + 1}/$groupedSize bulks - ${count + group.size}/$cardSize cards")
            }
          }
        } yield {
          count + group.size
        }
      }
      .map { _ => log.info(s"bulk finished !") }
  }


  def buildBody(group: Seq[A]): String

  protected def getId(card: MTGCard): String = {
    val id = card.publications.flatMap(_.multiverseId).headOption.getOrElse("na")
    norm(id + "-" + card.title)
  }

  protected def norm(string: String): String = StringUtils.normalize(string)
}
