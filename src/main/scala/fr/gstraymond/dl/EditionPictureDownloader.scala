package fr.gstraymond.dl

import java.io.{File, FileOutputStream}

import fr.gstraymond.model.MTGCard
import fr.gstraymond.scraper.GathererScraper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EditionPictureDownloader extends GathererScraper {

  val pictureLocation = "/media/guillaume/Data/Dropbox/Public/mtg/sets"
  val path = "/Handlers/Image.ashx?type=symbol&set={SET}&size={SIZE}&rarity={RARITY}"

  def download(cards: Seq[MTGCard]): Future[Unit] = Future.sequence {
    val tuples = cards.flatMap {
      _.publications.map { pub =>
        pub.stdEditionCode -> pub.rarityCode
      }
    }.distinct

    val editionToRarities = tuples.groupBy(_._1).mapValues(_.map(_._2))

    log.info(s"editionToRarities: $editionToRarities")

    editionToRarities.flatMap { case (edition, rarities) =>
      rarities.map { rarity =>
        val file = new File(s"$pictureLocation/$edition/$rarity.gif")

        if (! file.exists()) {
          getBytes(edition, rarity).map {
            case Array() => ()
            case bytes =>
              log.info(s"picture DLed: $edition-$rarity")
              if (! file.getParentFile.exists()) file.getParentFile.mkdirs()
              val fos = new FileOutputStream(file)
              fos.write(bytes)
              fos.close()
          }
        } else {
          Future.successful(())
        }
      }
    }
  }.map(_ => ())

  private def getBytes(edition: String, rarity: String): Future[Array[Byte]] = {
    get(buildUrl(edition, rarity, "large")).flatMap {
      case Array() => get(buildUrl(edition, rarity, "small"))
      case bytes => Future.successful(bytes)
    }
  }

  private def buildUrl(edition: String, rarity: String, size: String): String = {
    path.replace("{SET}", edition).replace("{RARITY}", rarity).replace("{SIZE}", size) // small
  }
}
