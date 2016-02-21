package fr.gstraymond.parser

import fr.gstraymond.constant.Color._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CardConverterColorsTest extends Specification {

  "card converter" should {
    "Nope" in {
      CardConverter._colors(None, Seq.empty) ===
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
      CardConverter._colors(Some("4"), Seq("White/Blue/Black/Red/Green color indicator")).sorted ===
        Seq(GREEN.lbl, WHITE.lbl, BLUE.lbl, BLACK.lbl, RED.lbl, GOLD, MULTICOLORED(5)).sorted
    }

    "Kozilek, the Great Distortion" in {
      _colors("8 C C") ===
        Seq(COLORLESS.lbl, UNCOLORED)
    }
  }

  private def _colors(cc: String) = CardConverter._colors(Some(cc), Seq.empty).sorted
}
