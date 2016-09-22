package fr.gstraymond.indexer

import java.io.File

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard
import fr.gstraymond.utils.Log

import scala.concurrent.Future

trait EsIndexer extends Log {

  val host = "localhost:9200"

  def index: String
  def `type`: String

  def indexPath = s"http://$host/$index"

  def bulkPath = s"$indexPath/${`type`}/_bulk"

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
    val file = new File(getClass.getResource(s"/indexer/$index.config.json").getFile)
    Http {
      url(indexPath).PUT <<< file OK as.String
    }.map { result =>
      log.info(s"configure: $result")
    }
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
              log.info(s"processed: ${i + 1}/$groupedSize bulks - ${count + group.size}/$cardSize cards")
            }
          }
        } yield {
          count + group.size
        }
      }
      .map { _ => log.info(s"bulk finished !") }
  }


  def buildBody(group: Seq[MTGCard]): String
}
