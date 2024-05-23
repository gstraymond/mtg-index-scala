package fr.gstraymond.indexer

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard
import fr.gstraymond.utils.Log
import fr.gstraymond.utils.StringUtils

import java.nio.charset.Charset
import scala.concurrent.Future
import scala.io.Source

trait EsIndexer[A] extends Log:

  val host = "localhost:9200"

  def index: String

  def indexPath = s"http://$host/$index"

  def bulkPath = s"$indexPath/_bulk"

  val bulk = 500

  def delete(): Future[Unit] =
    Http
      .default:
        url(indexPath).DELETE > as.String
      .map { result =>
        log.info(s"delete: $result")
        ()
      }

  def configure(): Future[Unit] =
    val body = Source.fromInputStream(getClass.getResourceAsStream(s"/indexer/$index.config.json")).mkString
    Http
      .default:
        url(indexPath).PUT
          .setContentType(
            "application/json",
            Charset.forName("utf-8")
          ) << body OK as.String
      .map { result =>
        log.info(s"configure: $result")
      }

  def index(elems: Seq[A]): Future[Unit] =
    val grouped     = elems.grouped(bulk).to(LazyList)
    val groupedSize = grouped.size
    val cardSize    = elems.size

    grouped.zipWithIndex
      .foldLeft(Future.successful(0)) { case (acc, (group, i)) =>
        for
          count <- acc
          _ <-
            Http
              .default:
                url(bulkPath).POST
                  .setContentType(
                    "application/json",
                    Charset.forName("utf-8")
                  ) << buildBody(group) OK as.String
              .map { _ =>
                log.info(s"processed: ${i + 1}/$groupedSize bulks - ${count + group.size}/$cardSize cards")
              }
        yield count + group.size
      }
      .map { _ => log.info(s"bulk finished !") }

  def buildBody(group: Seq[A]): String

  protected def getId(card: MTGCard): String =
    val id = card.publications.flatMap(_.multiverseId).headOption.getOrElse(-1L)
    norm(s"$id-${card.title}")

  protected def norm(string: String): String = StringUtils.normalize(string)
