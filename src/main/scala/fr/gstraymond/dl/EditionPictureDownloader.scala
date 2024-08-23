package fr.gstraymond.dl

import fr.gstraymond.constant.URIs
import fr.gstraymond.model.MTGCard
import fr.gstraymond.scraper.GathererScraper

import java.io.File
import java.io.FileOutputStream
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EditionPictureDownloader extends GathererScraper {

  val path = "/Handlers/Image.ashx?type=symbol&set={SET}&size={SIZE}&rarity={RARITY}"

  def download(cards: Seq[MTGCard]): Future[Unit] = Future
    .sequence {
      val tuples = cards.flatMap {
        _.publications.flatMap { pub =>
          pub.stdEditionCode -> pub.rarityCode match {
            case (Some(e), Some(r)) => Some(e -> r)
            case _                  => None
          }
        }
      }.distinct

      val editionToRarities = tuples.groupBy(_._1).view.mapValues(_.map(_._2)).toSeq

      editionToRarities.flatMap { case (edition, rarities) =>
        rarities.map { rarity =>
          val file = new File(s"${URIs.pictureLocation}/sets/$edition/$rarity.gif")

          if !file.exists() then
            getBytes(edition, rarity).map {
              case Array() => ()
              case bytes =>
                log.info(s"picture DLed: $edition-$rarity")
                if !file.getParentFile.exists() then { 
                  val _ = file.getParentFile.mkdirs()
                }
                val fos = new FileOutputStream(file)
                fos.write(bytes)
                fos.close()
            }
          else Future.successful(())
        }
      }
  }
    .map(_ => ())

  private def getBytes(edition: String, rarity: String): Future[Array[Byte]] =
    get(buildUrl(edition, rarity, "large")).flatMap {
      case Array() if rarity == "S" => get(buildUrl(edition, "R", "large"))
      case Array()                  => get(buildUrl(edition, rarity, "small"))
      case bytes                    => Future.successful(bytes)
    }

  private def buildUrl(edition: String, rarity: String, size: String): String =
    path.replace("{SET}", edition).replace("{RARITY}", rarity).replace("{SIZE}", size) // small
}
