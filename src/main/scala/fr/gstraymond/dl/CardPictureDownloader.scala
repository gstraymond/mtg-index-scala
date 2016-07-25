package fr.gstraymond.dl

import java.io.{File, FileOutputStream}

import fr.gstraymond.constant.URIs
import fr.gstraymond.model.{MTGCard, Publication}
import fr.gstraymond.scraper.{GathererScraper, MagicCardsInfoScraper}
import fr.gstraymond.utils.{Log, StringUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CardPictureDownloader extends GathererScraper with Log {

  def download(cards: Seq[MTGCard]): Future[Unit] = {
    val init = Future.successful(())
    cards.flatMap(download).foldLeft(init) { (acc, f) =>
      for {
        _ <- acc
        _ <- f
      } yield {
        ()
      }
    }
  }

  def download(card: MTGCard): Seq[Future[Unit]] = {
    card.publications.map { publication =>
      publication.multiverseId.map { multiverseId =>
        val file = new File(s"${URIs.pictureLocation}/images/${publication.editionCode}/${formatTitle(card)}")

        if (!file.getParentFile.exists()) file.getParentFile.mkdirs()

        if (!file.exists()) {
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
        } else {
          Future.successful(())
        }
      }.getOrElse {
        Future.successful(())
      }
    }
  }


  def formatTitle(card: MTGCard) = {
    def name = StringUtils.normalize(card.title)
    s"$name.jpg"
  }
}
