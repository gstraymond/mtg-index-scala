package fr.gstraymond.parser.field

import fr.gstraymond.parser.PriceModels._

trait PriceRangesField:

  def _priceRanges(prices: Seq[CardPrice]): Seq[String] =
    (getPaperPrices(prices) ++ getMtgoPrices(prices)).map(toRange).toList

  def _minPaperPriceRange(prices: Seq[CardPrice]): Option[String] =
    getPaperPrices(prices).minOption.map(toRange)

  def _minMtgoPriceRange(prices: Seq[CardPrice]): Option[String] =
    getMtgoPrices(prices).minOption.map(toRange)

  private def toRange(price: Double) = price match
    case p if p < 0.20              => "< 0.20$"
    case p if p >= 0.20 && p < 0.50 => "0.20$ .. 0.50$"
    case p if p >= 0.50 && p < 1    => "0.50$ .. 1$"
    case p if p >= 1 && p < 5       => "1$ .. 5$"
    case p if p >= 5 && p < 20      => "5$ .. 20$"
    case p if p >= 20 && p < 100    => "20$ .. 100$"
    case _                          => "> 100$"

  private def getPaperPrices(prices: Seq[CardPrice]): Set[Double] =
    prices
      .flatMap { _.paper }
      .flatMap { price => price.normal ++ price.foil }
      .toSet

  private def getMtgoPrices(prices: Seq[CardPrice]): Set[Double] =
    prices
      .flatMap { _.online }
      .flatMap { price => price.normal ++ price.foil }
      .toSet
