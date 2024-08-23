package fr.gstraymond.parser

import fr.gstraymond.parser.field.ColorField
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scala.annotation.nowarn

@nowarn
@RunWith(classOf[JUnitRunner])
class CardConverterDualColorsTest extends Specification with ColorField {

  "card converter" should {
    "Nope" in {
      _dualColors(None, None) ===
        Nil
    }

    "Zuran Spellcaster" in {
      _dualColors("2 U").sorted ===
        Seq("Black OR Blue", "Blue OR Green", "Blue OR Red", "Blue OR White").sorted
    }

    "Zhou Yu, Chief Commander" in {
      _dualColors("5 U U").sorted ===
        Seq("Black OR Blue", "Blue OR Green", "Blue OR Red", "Blue OR White").sorted
    }

    "Yore-Tiller Nephilim" in {
      _dualColors("W U B R") === Nil
    }

    "Arsenal Thresher" in {
      _dualColors("2 WB U") === Nil
    }

    "Fireball" in {
      _dualColors("X R") ===
        Seq("Black OR Red", "Blue OR Red", "Green OR Red", "Red OR White").sorted
    }

    "Reaper King" in {
      _dualColors("2/W 2/U 2/B 2/R 2/G") === Nil
    }

    "Act of Aggression" in {
      _dualColors("3 RP RP") ===
        Seq("Black OR Red", "Blue OR Red", "Green OR Red", "Red OR White").sorted
    }

    "Decree of Justice" in {
      _dualColors("X X 2 W W") ===
        Seq("Black OR White", "Blue OR White", "Green OR White", "Red OR White").sorted
    }

    "Emrakul, the Aeons Torn" in {
      _dualColors("15") === Nil
    }

    "Autochthon Wurm" in {
      _dualColors("10 G G G W W") === Seq("Green OR White")
    }

    "Transguild Courier" in {
      _dualColors(Some("4"), None) === Nil
    }

    "Transguild Courier - new" in {
      _dualColors(Some("4"), Some(Seq("White", "Blue", "Black", "Red", "Green"))) === Nil
    }

    "Kozilek, the Great Distortion" in {
      _dualColors("8 C C") === Nil
    }

    "Abstruse Interference" in {
      _dualColors(Some("2 U"), None).sorted ===
        Seq("Black OR Blue", "Blue OR Green", "Blue OR Red", "Blue OR White").sorted
    }

    "Ludevic's Abomination" in {
      _dualColors(None, Some(Seq("Blue"))).sorted ===
        Seq("Black OR Blue", "Blue OR Green", "Blue OR Red", "Blue OR White").sorted
    }

    "Dralnu, seigneur liche" in {
      _dualColors(Some("3 U B"), Some(Seq("Blue", "Black"))).sorted === Seq("Black OR Blue")
    }
  }

  private def _dualColors(cc: String): Seq[String] = _dualColors(Some(cc), None).sorted
}
