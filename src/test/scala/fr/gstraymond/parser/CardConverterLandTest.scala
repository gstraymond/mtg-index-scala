package fr.gstraymond.parser

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CardConverterLandTest extends Specification {

  "card converter" should {

    "Tundra" in {
      _land(
        "Land — Plains Island",
        "({T}: Add {W} or {U} to your mana pool.)"
      ) === Seq("Dual Land", "Dual Basic Land", "Produce Blue Mana", "Produce White Mana")
    }

    "Gavony Township" in {
      _land(
        "Land",
        "{T}: Add {C} to your mana pool.",
        "{2}{G}{W}, {T}: Put a +1/+1 counter on each creature you control."
      ) === Seq("Produce Colorless Mana")
    }

    "Greypelt Refuge" in {
      _land(
        "Land",
        "Graypelt Refuge enters the battlefield tapped.",
        "When Graypelt Refuge enters the battlefield, you gain 1 life.",
        "{T}: Add {G} or {W} to your mana pool."
      ) === Seq("Dual Land", "Produce Green Mana", "Produce White Mana")
    }

    "Jungle Shrine" in {
      _land(
        "Land",
        "Jungle Shrine enters the battlefield tapped.",
        "{T}: Add {R}, {G}, or {W} to your mana pool."
      ) === Seq("Triple Land", "Produce Red Mana", "Produce Green Mana", "Produce White Mana")
    }

    "Crystal Quarry" in {
      _land(
        "Land",
        "{T}: Add {C} to your mana pool.",
        "{5}, {T}: Add {W}{U}{B}{R}{G} to your mana pool."
      ) === Seq("Produce Black Mana", "Produce Blue Mana", "Produce Red Mana", "Produce Green Mana", "Produce White Mana", "Produce Colorless Mana")
    }

    "Misty Rainforest" in {
      _land(
        "Land",
        "{T}, Pay 1 life, Sacrifice Misty Rainforest: Search your library for a Forest or Island card and put it onto the battlefield. Then shuffle your library."
      ) === Seq("Fetch land")
    }

    "Bad River" in {
      _land(
        "Land",
        "Bad River enters the battlefield tapped.",
        "{T}, Sacrifice Bad River: Search your library for an Island or Swamp card and put it onto the battlefield. Then shuffle your library."
      ) === Seq("Fetch land")
    }

    "Blinkmoth Nexus" in {
      _land(
        "Land",
        "{T}: Add {C} to your mana pool.",
        "{1}: Blinkmoth Nexus becomes a 1/1 Blinkmoth artifact creature with flying until end of turn. It's still a land.",
        "{1}, {T}: Target Blinkmoth creature gets +1/+1 until end of turn."
      ) === Seq("Man land", "Produce Colorless Mana")
    }

    "Swamp" in {
      _land(
        "Basic Land -- Swamp"
      ) === Seq("Produce Black Mana")
    }
  }

  private def _land(`type`: String, description: String*) =
    CardConverter._land(`type`, description)
}
