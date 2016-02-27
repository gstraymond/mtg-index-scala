package fr.gstraymond.dl

import java.io.{File, FileOutputStream}

import fr.gstraymond.model.{MTGCard, Publication}
import fr.gstraymond.scraper.MagicCardsInfoScraper
import fr.gstraymond.utils.{Log, StringUtils}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CardPictureDownloader extends MagicCardsInfoScraper with Log {

  val pictureLocation = "/media/guillaume/Data/Dropbox/Public/mtg/pics"
  //val pictureHost = "http://dl.dropboxusercontent.com/u/22449802/mtg"

  def download(cards: Seq[MTGCard]): Future[Unit] = Future.sequence {
    cards.flatMap(download)
  }.map(_ => ())

  def download(card: MTGCard): Seq[Future[Unit]] = {
    card.publications.map { publication =>
      val file = new File(s"$pictureLocation/${publication.editionCode}/${formatTitle(publication, card)}")

      if (! file.getParentFile.exists()) file.getParentFile.mkdirs()

      if (!file.exists()) {
        log.warn(s"picture not found: [${file.getAbsoluteFile}] ${card.title} - ${publication.edition}")

        val path = s"/scans/en/${publication.editionCode}/${publication.collectorNumber}.jpg"
        get(path).map { bytes =>
          val fos = new FileOutputStream(file)
          fos.write(bytes)
          fos.close()
        }
      } else {
        Future.successful(())
      }
    }
  }


  def formatTitle(publication: Publication, card: MTGCard) = {
    def name = StringUtils.normalize(card.title)
    s"${publication.collectorNumber}-$name.jpg"
  }
}
