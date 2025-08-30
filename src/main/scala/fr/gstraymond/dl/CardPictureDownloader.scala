package fr.gstraymond.dl

import fr.gstraymond.constant.URIs
import fr.gstraymond.model.MTGCard
import fr.gstraymond.scraper.GathererScraper
import fr.gstraymond.utils.Log
import fr.gstraymond.utils.StringUtils

import java.io.File
import java.io.FileOutputStream
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CardPictureDownloader extends GathererScraper with Log {

  def download(cards: Seq[MTGCard]): Future[Unit] = {
    val init = Future.unit
    cards.foldLeft(init) { (acc, f) =>
      acc.flatMap(_ => download(f))
    }
  }

  def download(card: MTGCard): Future[Unit] = {
    card.publications.foldLeft(Future.unit) { case (acc, publication) =>
      val res = publication.multiverseId
        .map { multiverseId =>
          val file =
            new File(s"${URIs.pictureLocation}/pics/${publication.editionCode}/$multiverseId-${formatTitle(card)}")

          if !file.getParentFile.exists() then {
            val _ = file.getParentFile.mkdirs()
          }

          if !file.exists() then {
            log.warn(s"picture not found: [${file.getAbsoluteFile}] ${card.title} - ${publication.edition}")
            Thread.sleep(100)
            val path = s"/Handlers/Image.ashx?multiverseid=$multiverseId&type=card"
            get(path).map {
              case Array() =>
              case bytes =>
                val fos = new FileOutputStream(file)
                fos.write(bytes)
                fos.close()
            }
          }
          else Future.unit
        }
        .getOrElse {
          Future.unit
      }

      acc.flatMap(_ => res)
    }
  }

  def formatTitle(card: MTGCard): String = {
    def name = StringUtils.normalize(card.title)
    s"$name.jpg"
  }
}
