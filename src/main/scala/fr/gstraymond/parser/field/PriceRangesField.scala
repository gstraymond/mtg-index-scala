package fr.gstraymond.parser.field

import fr.gstraymond.parser.PriceModels._

trait PriceRangesField {

  def _priceRanges(prices: Seq[CardPrice]): Seq[String] = getPrices(prices).map {
    case p if p < 0.20              => "< 0.20$"
    case p if p >= 0.20 && p < 0.50 => "0.20$ .. 0.50$"
    case p if p >= 0.50 && p < 1    => "0.50$ .. 1$"
    case p if p >= 1 && p < 5       => "1$ .. 5$"
    case p if p >= 5 && p < 20      => "5$ .. 20$"
    case p if p >= 20 && p < 100    => "20$ .. 100$"
    case _                          => "> 100$"
  }.toList

  private def getPrices(prices: Seq[CardPrice]): Set[Double] =
    prices
      .flatMap { price => price.online ++ price.paper }
      .flatMap { price => price.normal ++ price.foil }
      .toSet
}
