package fr.gstraymond.indexer

import fr.gstraymond.model.MTGCard
import fr.gstraymond.scraper.Sttp
import fr.gstraymond.utils.Log
import fr.gstraymond.utils.StringUtils
import sttp.client4.quick.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

trait EsIndexer[A] extends Log {

  val host = "localhost:9200"

  def index: String

  def indexPath = s"http://$host/$index"

  def bulkPath = s"$indexPath/_bulk"

  val bulk = 500

  def delete(): Future[Unit] =
    Sttp.delete(indexPath).map { result =>
      log.info(s"delete: $result")
      ()
    }

  def configure(): Future[Unit] = {
    val body = Source.fromInputStream(getClass.getResourceAsStream(s"/indexer/$index.config.json")).mkString
    Sttp
      .putJson(indexPath, body)
      .map { result =>
        log.info(s"configure: $result")
      }
  }

  def index(elems: Seq[A]): Future[Unit] = {
    val grouped     = elems.grouped(bulk).to(LazyList)
    val groupedSize = grouped.size
    val cardSize    = elems.size

    grouped.zipWithIndex
      .foldLeft(Future.successful(0)) { case (acc, (group, i)) =>
        for {
          count <- acc
          _ <- Sttp
            .postJson(bulkPath, buildBody(group))
            .map { _ =>
              log.info(s"processed: ${i + 1}/$groupedSize bulks - ${count + group.size}/$cardSize cards")
            }
        }
        yield count + group.size
      }
      .map { _ => log.info(s"bulk finished !") }
  }

  def buildBody(group: Seq[A]): String

  protected def getId(card: MTGCard): String = {
    val id = card.publications.flatMap(_.multiverseId).headOption.orElse(card.publications.flatMap(_.scryfallId).headOption).getOrElse(-1L)
    norm(s"$id-${card.title}")
  }

  protected def norm(string: String): String = StringUtils.normalize(string)
}
