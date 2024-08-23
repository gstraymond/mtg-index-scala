package fr.gstraymond.parser.field

import fr.gstraymond.constant.Land

trait SpecialField {

  case class SpecialCard(title: String, `type`: String, description: Seq[String])

  // http://mtgsalvation.gamepedia.com/Mill

  private val specialFilters: Seq[(SpecialCard => Boolean, Seq[String])] = Seq(
    isVanilla         -> Seq("Vanilla"),
    isPower9          -> Seq("Power 9"),
    isTuck            -> Seq("Tuck"),
    isTutor           -> Seq("Tutor"),
    isUncounterable   -> Seq("Uncounterable"),
    isVenom           -> Seq("Venom"),
    isLandDestruction -> Seq("Land Destruction"),
    isLord            -> Seq("Lord"),
    isWrathEffect     -> Seq("Wrath Effect"),
    isMill            -> Seq("Mill"),
    isManaDork        -> Seq("Mana Dork"),
    isManaRock        -> Seq("Mana Rock")
  )

  def _special(title: String, `type`: String, description: Seq[String]): Seq[String] = {
    val card = SpecialCard(title, `type`, description)
    specialFilters.flatMap {
      case (filter, specials) if filter(card) => specials
      case _                                  => Seq.empty
    }.distinct
  }

  private def isVanilla(card: SpecialCard) =
    card.`type`.contains("Creature") &&
      card.description.mkString.isEmpty

  private val power9 = Seq(
    "Ancestral Recall",
    "Black Lotus",
    "Mox Emerald",
    "Mox Jet",
    "Mox Pearl",
    "Mox Ruby",
    "Mox Sapphire",
    "Timetwister",
    "Time Walk"
  )

  private def isPower9(card: SpecialCard): Boolean = power9.contains(card.title)

  private val tuckKeywords = Seq("put ", " on the bottom", "owner's library.")

  private def isTuck(card: SpecialCard) =
    card.description.exists { line =>
      tuckKeywords.forall(line.toLowerCase.contains)
    }

  private val tutorKeywords = Seq(
    Seq("Search your library for ", "reveal", "into your hand", "shuffle your library"),
    Seq("choose ", "from outside the game", "reveal", "into your hand")
  )

  private def isTutor(card: SpecialCard) =
    card.description.exists { line =>
      tutorKeywords.exists(_.forall(line.contains))
    }

  private val uncounterableKeywords = "can't be countered"

  private def isUncounterable(card: SpecialCard) =
    card.description.exists {
      _.contains(uncounterableKeywords)
    }

  private val venomKeywords = Seq(
    Seq("blocks or becomes blocked by ", "destroy"),
    Seq("deals combat damage to a creature", "destroy")
  )

  private def isVenom(card: SpecialCard) =
    card.title != "Treefolk Mystic" &&
      card.description.exists { line =>
        venomKeywords.exists(_.forall(line.contains))
      }

  private val landDestructionKeywords = "Destroy target"

  private def isLandDestruction(card: SpecialCard) =
    card.description.exists { line =>
      line.contains(landDestructionKeywords) &&
      (Land.ALL.keys.exists(line.contains) ||
        line.contains(" land"))
    }

  private val lordKeywords = Seq(
    Seq("creatures ", " get "),
    Seq("creatures ", " have "),
    Seq(" cast ", " cost ")
  )

  private val lordNonKeywords = Seq(
    "enters the battlefield",
    "additional cost",
    ": ",
    "whenever",
    "prowl"
  )

  private def isLord(card: SpecialCard) =
    card.`type`.contains("Creature ? ") `&&` {
      val subTypes = card.`type`.toLowerCase.split(" ? ")(1).split(" ");
      card.description.map(_.toLowerCase).exists { line =>
        subTypes.exists(line.contains) &&
        lordNonKeywords.forall(!line.contains(_)) &&
        lordKeywords.exists(_.forall(line.contains))
      }
    }

  private val wrathEffectKeywords = Seq(
    Seq("Destroy all ", " lands "),
    Seq("Destroy all ", " creatures "),
    Seq("Return all ", " lands "),
    Seq("Return all ", " creatures "),
    Seq(" creatures get -", "/-", "until end of turn")
  )

  private def isWrathEffect(card: SpecialCard) =
    !card.`type`.startsWith("Enchantment") &&
      card.description.exists { line =>
        wrathEffectKeywords.exists(_.forall(line.contains)) &&
        !line.contains(" blocked ") &&
        !line.contains("0") &&
        !line.contains("1")
      }

  private val millKeywords = Seq(
    Seq(" put", " the top ", " of his or her library", " into his or her graveyard"),
    Seq(" put", " the top ", " of their library", " into their graveyard"),
    Seq("Exile the top ", "opponent", " library"),
    Seq("reveals cards from the top of his or her library", "puts those cards into his or her graveyard"),
    Seq("reveals cards from the top of their library", "puts those cards into their graveyard")
  )

  private def isMill(card: SpecialCard) =
    card.description.exists { line =>
      millKeywords.exists(_.forall(line.contains))
    }

  private val manaDork = Seq(
    Seq(":", "Add", "{G}", "."),
    Seq(":", "Add", "{U}", "."),
    Seq(":", "Add", "{R}", "."),
    Seq(":", "Add", "{B}", "."),
    Seq(":", "Add", "{W}", "."),
    Seq(":", "Add", "{C}", "."),
    Seq(":", "Add", "mana", ".")
  )

  private def isManaDork(card: SpecialCard) =
    card.`type`.contains("Creature") &&
      card.description.exists { line =>
        manaDork.exists(_.forall(line.contains))
      }

  private def isManaRock(card: SpecialCard) =
    card.`type`.contains("Artifact") &&
      card.description.exists { line =>
        manaDork.exists(_.forall(line.contains))
      }
}
