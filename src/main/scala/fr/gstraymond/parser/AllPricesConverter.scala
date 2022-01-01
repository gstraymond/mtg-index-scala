package fr.gstraymond.parser

import fr.gstraymond.model.MTGJsonAllPrices
import fr.gstraymond.parser.PriceModels._

object AllPricesConverter {

  def convert(prices: MTGJsonAllPrices): Seq[CardPrice] =
    prices.data.map { case (uuid, price) =>
      val paper = price.paper.map { paper =>
        val maybeRetail = paper.cardkingdom.flatMap(_.retail).orElse(paper.tcgplayer.flatMap(_.retail))
        val normal      = maybeRetail.flatMap(_.normal).map(_.head._2)
        val foil        = maybeRetail.flatMap(_.foil).map(_.head._2)
        Price(normal, foil)
      }
      val online = price.mtgo.map { online =>
        val maybeRetail = online.cardhoarder.retail
        val normal      = maybeRetail.flatMap(_.normal).map(_.head._2)
        val foil        = maybeRetail.flatMap(_.foil).map(_.head._2)
        Price(normal, foil)
      }
      CardPrice(uuid, paper, online)
    }.toSeq
}
