package fr.gstraymond.parser

import fr.gstraymond.model.RawCard
import fr.gstraymond.utils.Log

object OracleRawCardConverter extends Log {

  val VALID_CASTING_COST = "X*d*[(,),/,r,g,b,u,w,p,2]*[R,G,B,U,W]*".r

  def convert(cards: Seq[Seq[String]]): Seq[RawCard] = {
    cards.map(convertCard)
  }

  def convertCard(card: Seq[String]): RawCard = {
    val (maybeTitle, c1) = title(card)
    val (maybeRarityEdition, c2) = rarityEdition(c1)
    val (maybeCastingCost, c3) = castingCost(c2)
    val (maybeType, c4) = `type`(c3)
    val (maybePT, c5) = powerToughness(c4, maybeType)
    val description = c5

    RawCard(
      maybeTitle,
      maybeCastingCost,
      maybeType,
      maybePT,
      description,
      maybeRarityEdition
    )
  }

  def title = first _

  def rarityEdition = last _

  def castingCost(card: Seq[String]) = card.head match {
    case VALID_CASTING_COST() => first(card)
    case _ =>
      log.info(s"${card.head} didn't match $VALID_CASTING_COST")
      None -> card
  }

  def `type` = first _

  def powerToughness(card: Seq[String], maybeType: Option[String]) = maybeType match {
    case Some(t) if t.contains("Creature") => first(card)
    case _ => None -> card
  }


  def first(card: Seq[String]) = Some(card.head) -> card.tail

  def last(card: Seq[String]) = Some(card.reverse.head) -> card.reverse.tail.reverse
}
