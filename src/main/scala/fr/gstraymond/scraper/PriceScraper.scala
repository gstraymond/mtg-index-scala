package fr.gstraymond.scraper

import fr.gstraymond.model.ScrapedPrice

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PriceScraper extends MTGGoldFishScraper {

  val path = "/prices/select"
  val sep = " - "

  def scrap: Future[Seq[ScrapedPrice]] = {
    scrapEditionUrls.flatMap { editionUrls =>
      val init: Future[Seq[ScrapedPrice]] = Future.successful(Nil)
      editionUrls.foldLeft(init) { (acc, editionUrl) =>
        for {
          prices <- acc
          _ = Thread.sleep(100)
          eventualPrices = scrapEditionPrices(editionUrl)
          eventualFoilPrices = scrapEditionPrices(editionUrl + "_F", foil = true)
          newPrices <- eventualPrices
          newPricesFoil <- eventualFoilPrices
        } yield {
          prices ++ mergePrices(newPrices, newPricesFoil)
        }
      }
    }
  }

  private def mergePrices(newPrices: Seq[ScrapedPrice],
                          newPricesFoil: Seq[ScrapedPrice]): Seq[ScrapedPrice] = {
    (newPrices ++ newPricesFoil).groupBy(_.card).values.toSeq.map {
      case Seq(p) => p
      case Seq(p1, p2) => p1.copy(
        price = p1.price.orElse(p2.price),
        foilPrice = p1.foilPrice.orElse(p2.foilPrice)
      )
    }
  }

  def scrapEditionUrls: Future[Seq[String]] = {
    scrap(path).map { doc =>
      doc
        .select("div.priceList-selectMenu li[role=presentation]").asScala
        .filter(_.select("a img").asScala.nonEmpty)
        .map { li =>
          li.select("a").asScala.head.attr("href")
        }
    }
  }

  def scrapEditionPrices(path: String, foil: Boolean = false): Future[Seq[ScrapedPrice]] = {
    def parseDouble(str: String) = str.replace(",", "").toDouble

    scrap(path).map { doc =>
      val editionCode = path.split("/").last.toLowerCase
      val editionName = doc.select(".price-card-name-header-name").text()
      doc
        .select("div.index-price-table-paper table tr").asScala
        .filter(_.select("td").asScala.nonEmpty)
        .map { tr =>
          tr.select("td").asScala match {
            case Seq(card, _, _, price, _, _, _, _) =>
              ScrapedPrice(
                card.text(),
                editionCode match {
                  case _ if foil => editionCode.dropRight(" f".length)
                  case _ => editionCode
                },
                editionName match {
                  case _ if foil => editionName.dropRight(" Foils".length)
                  case _ => editionName
                },
                if (foil) None else Some(parseDouble(price.text())),
                if (foil) Some(parseDouble(price.text())) else None
              )
          }
        }
    }.recover { case e: Exception =>
      log.error(s"error parsing $host $path -- ${e.getMessage}")
      Nil
    }
  }
}
