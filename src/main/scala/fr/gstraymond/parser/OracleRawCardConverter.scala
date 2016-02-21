package fr.gstraymond.parser

import fr.gstraymond.model.RawCard
import fr.gstraymond.utils.Log

object OracleRawCardConverter extends Log {

  val VALID_CASTING_COST = "X*\\d*[(,),/,r,g,b,u,w,p,2]*[R,G,B,U,W,C]*".r

  val SPECIAL_CASTING_COSTS = Seq(
    "1,000,000", "W(½)", "XYZRR"
  )

  def convert(cards: Seq[Seq[String]]): Seq[RawCard] = {
    cards
      .filterNot(_.head.contains(" // ")) // double cards are duplicate
      .map(convertCard)
  }

  def convertCard(card: Seq[String]): RawCard = {
    val (maybeTitle, c1) = title(card)
    val (maybeRarityEdition, c2) = rarityEdition(c1)
    val (maybeCastingCost, c3) = castingCost(c2, maybeTitle)
    val (maybeType, c4) = `type`(c3)
    val (maybePT, c5) = powerToughness(c4, maybeType)
    val description = c5

    RawCard(
      maybeTitle,
      maybeCastingCost,
      maybeType,
      maybePT,
      description,
      maybeRarityEdition.map(_.split(", ").toSeq).getOrElse(Seq.empty)
    )
  }

  def title = first _

  def rarityEdition = last _

  def castingCost(card: Seq[String], maybeTitle: Option[String]) = card.head match {
    case "Scheme" | "Ongoing Scheme" | "Vanguard" | "Phenomenon" | "Conspiracy" => nope(card)
    case plane if plane.contains("Plane") => nope(card)
    case land if land.contains("Land") => nope(card)
    case _ if card.exists(_.contains("Back face")) => nope(card)
    case _ if card.exists(_.contains("Suspend ")) => nope(card)
    case _ if card.exists(_.contains("Nonexistent mana costs")) => nope(card)
    case VALID_CASTING_COST() => first(card)
    case cc if SPECIAL_CASTING_COSTS.contains(cc) => first(card)
    case _ =>
      log.warn(s"$VALID_CASTING_COST didn't match [${card.head}] --- ${maybeTitle.getOrElse("")} ")
      nope(card)
  }

  def `type` = first _

  def powerToughness(card: Seq[String], maybeType: Option[String]) = maybeType match {
    case Some(t) if t.contains("Creature -- ") => first(card)
    case _ => nope(card)
  }

  def first(card: Seq[String]) = Some(card.head) -> card.tail

  def last(card: Seq[String]) = Some(card.reverse.head) -> card.reverse.tail.reverse

  def nope(card: Seq[String]) = None -> card
}
