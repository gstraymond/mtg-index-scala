package fr.gstraymond.parser.field

import fr.gstraymond.constant.Land

trait LandField:

  case class LandCard(`type`: String, description: Seq[String])

  private val landFilters: Seq[(LandCard => Boolean, Seq[String])] = Seq(
    countLandTypes(2)   -> Seq("Dual Land", "Dual Basic Land"),
    countLandColors(2)  -> Seq("Dual Land"),
    countLandColors(3)  -> Seq("Triple Land"),
    isFetchLand         -> Seq("Fetch land"),
    isManLand           -> Seq("Man land"),
    canLandProduce("B") -> Seq("Produce Black Mana"),
    canLandProduce("U") -> Seq("Produce Blue Mana"),
    canLandProduce("R") -> Seq("Produce Red Mana"),
    canLandProduce("G") -> Seq("Produce Green Mana"),
    canLandProduce("W") -> Seq("Produce White Mana"),
    canLandProduce("C") -> Seq("Produce Colorless Mana")
  )

  def _land(`type`: String, description: Seq[String]): Seq[String] =
    val card = LandCard(`type`, description)
    if isLand(card) then
      landFilters
        .foldLeft(Seq.empty[String]) { case (acc, (filter, specials)) =>
          acc ++ (if filter(card) then specials else Seq.empty)
        }
        .distinct
    else Seq.empty

  private def countLandTypes(count: Int)(card: LandCard) =
    Land.ALL.keys.count(card.`type`.contains) == count

  private def countLandColors(count: Int)(card: LandCard) =
    card.description.map { line =>
      Land.ALL.values.count(landProduce(line, _))
    }.sum == count

  private def landProduce(line: String, c: String): Boolean =
    if line.contains("{T}:") then line.split("\\{T\\}:")(1).contains(s"{$c}")
    else false

  private val fetchLandKeywords = Seq("Sacrifice", "Search your library", "put it onto the battlefield")

  private def isFetchLand(card: LandCard) =
    fetchLandKeywords.forall(card.description.mkString.contains) &&
      Land.ALL.keys.count(card.description.mkString.contains) > 0

  private val manLandKeywords = Seq(" becomes ", " creature ")

  private def isManLand(card: LandCard) =
    manLandKeywords.forall(card.description.mkString.contains)

  private val produceLandKeywords = Seq("Add {", "}.")

  private def canLandProduce(color: String)(card: LandCard) =
    (card.`type`.contains("Basic") && card.`type`.contains(Land.REV.getOrElse(color, "???"))) ||
      card.description.exists { line =>
        produceLandKeywords.forall(line.contains) && landProduce(line, color)
      }

  private def isLand(card: LandCard) = card.`type`.contains("Land")
