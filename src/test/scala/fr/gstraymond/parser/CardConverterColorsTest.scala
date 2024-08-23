package fr.gstraymond.parser

import fr.gstraymond.constant.Color._
import fr.gstraymond.parser.field.ColorField
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scala.annotation.nowarn

@nowarn
@RunWith(classOf[JUnitRunner])
class CardConverterColorsTest extends Specification with ColorField {

  "card converter" should {
    "Nope" in {
      _colors(None, Seq.empty, None) ===
        Seq(UNCOLORED)
    }

    "Zuran Spellcaster" in {
      _colors("2 U") ===
        Seq(BLUE.lbl, MONOCOLORED).sorted
    }

    "Zhou Yu, Chief Commander" in {
      _colors("5 U U") ===
        Seq(BLUE.lbl, MONOCOLORED).sorted
    }

    "Yore-Tiller Nephilim" in {
      _colors("W U B R") ===
        Seq(BLUE.lbl, BLACK.lbl, RED.lbl, WHITE.lbl, GOLD, MULTICOLORED(4)).sorted
    }

    "Arsenal Thresher" in {
      _colors("2 WB U") ===
        Seq(BLUE.lbl, BLACK.lbl, WHITE.lbl, GOLD, GUILD, MULTICOLORED(3)).sorted
    }

    "Fireball" in {
      _colors("X R") ===
        Seq(X.lbl, RED.lbl, MONOCOLORED).sorted
    }

    "Reaper King" in {
      _colors("2/W 2/U 2/B 2/R 2/G") ===
        Seq(BLUE.lbl, BLACK.lbl, RED.lbl, WHITE.lbl, GREEN.lbl, GOLD, MULTICOLORED(5)).sorted
    }

    "Act of Aggression" in {
      _colors("3 RP RP") ===
        Seq(LIFE.lbl, RED.lbl, MONOCOLORED).sorted
    }

    "Decree of Justice" in {
      _colors("X X 2 W W") ===
        Seq(X.lbl, WHITE.lbl, MONOCOLORED).sorted
    }

    "Emrakul, the Aeons Torn" in {
      _colors("15") ===
        Seq(UNCOLORED)
    }

    "Autochthon Wurm" in {
      _colors("10 G G G W W") ===
        Seq(GREEN.lbl, WHITE.lbl, GOLD, MULTICOLORED(2)).sorted
    }

    "Transguild Courier" in {
      _colors(Some("4"), Seq("White/Blue/Black/Red/Green color indicator"), None).sorted ===
        Seq(GREEN.lbl, WHITE.lbl, BLUE.lbl, BLACK.lbl, RED.lbl, GOLD, MULTICOLORED(5)).sorted
    }

    "Transguild Courier - new" in {
      _colors(Some("4"), Seq.empty, Some(Seq("White", "Blue", "Black", "Red", "Green"))).sorted ===
        Seq(GREEN.lbl, WHITE.lbl, BLUE.lbl, BLACK.lbl, RED.lbl, GOLD, MULTICOLORED(5)).sorted
    }

    "Kozilek, the Great Distortion" in {
      _colors("8 C C") ===
        Seq(COLORLESS.lbl, UNCOLORED)
    }

    "Abstruse Interference" in {
      _colors(Some("2 U"), Seq("Devoid (This card has no color.)"), None).sorted ===
        Seq(UNCOLORED)
    }

    "Ludevic's Abomination" in {
      _colors(None, Seq.empty, Some(Seq("Blue"))).sorted ===
        Seq(MONOCOLORED, BLUE.lbl)
    }

    "Dralnu, seigneur liche" in {
      _colors(Some("3 U B"), Seq.empty, Some(Seq("Blue", "Black"))).sorted ===
        Seq(BLACK.lbl, BLUE.lbl, GOLD, MULTICOLORED(2)).sorted
    }
  }

  private def _colors(cc: String): Seq[String] = _colors(Some(cc), Seq.empty, None).sorted
}
