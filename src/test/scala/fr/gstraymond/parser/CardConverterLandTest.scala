package fr.gstraymond.parser

import fr.gstraymond.parser.field.LandField
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scala.annotation.nowarn

@nowarn
@RunWith(classOf[JUnitRunner])
class CardConverterLandTest extends Specification with LandField:

  "card converter" should:

    "Tundra" in:
      land(
        "Land ? Plains Island",
        "({T}: Add {W} or {U}.)"
      ) === Seq("Dual Land", "Dual Basic Land", "Produce Blue Mana", "Produce White Mana")

    "Gavony Township" in:
      land(
        "Land",
        "{T}: Add {C}.",
        "{2}{G}{W}, {T}: Put a +1/+1 counter on each creature you control."
      ) === Seq("Produce Colorless Mana")

    "Greypelt Refuge" in:
      land(
        "Land",
        "Graypelt Refuge enters the battlefield tapped.",
        "When Graypelt Refuge enters the battlefield, you gain 1 life.",
        "{T}: Add {G} or {W}."
      ) === Seq("Dual Land", "Produce Green Mana", "Produce White Mana")

    "Jungle Shrine" in:
      land(
        "Land",
        "Jungle Shrine enters the battlefield tapped.",
        "{T}: Add {R}, {G}, or {W}."
      ) === Seq("Triple Land", "Produce Red Mana", "Produce Green Mana", "Produce White Mana")

    "Crystal Quarry" in:
      land(
        "Land",
        "{T}: Add {C}.",
        "{5}, {T}: Add {W}{U}{B}{R}{G}."
      ) === Seq(
        "Produce Black Mana",
        "Produce Blue Mana",
        "Produce Red Mana",
        "Produce Green Mana",
        "Produce White Mana",
        "Produce Colorless Mana"
      )

    "Misty Rainforest" in:
      land(
        "Land",
        "{T}, Pay 1 life, Sacrifice Misty Rainforest: Search your library for a Forest or Island card and put it onto the battlefield. Then shuffle your library."
      ) === Seq("Fetch land")

    "Bad River" in:
      land(
        "Land",
        "Bad River enters the battlefield tapped.",
        "{T}, Sacrifice Bad River: Search your library for an Island or Swamp card and put it onto the battlefield. Then shuffle your library."
      ) === Seq("Fetch land")

    "Blinkmoth Nexus" in:
      land(
        "Land",
        "{T}: Add {C}.",
        "{1}: Blinkmoth Nexus becomes a 1/1 Blinkmoth artifact creature with flying until end of turn. It's still a land.",
        "{1}, {T}: Target Blinkmoth creature gets +1/+1 until end of turn."
      ) === Seq("Man land", "Produce Colorless Mana")

    "Swamp" in:
      land(
        "Basic Land -- Swamp"
      ) === Seq("Produce Black Mana")

  private def land(`type`: String, description: String*): Seq[String] =
    _land(`type`, description)
