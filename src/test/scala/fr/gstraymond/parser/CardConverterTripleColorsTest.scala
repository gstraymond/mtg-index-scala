package fr.gstraymond.parser

import fr.gstraymond.parser.field.ColorField
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CardConverterTripleColorsTest extends Specification with ColorField {

  "card converter" should {
    "Nope" in {
      _tripleColors(None, None) === Nil
    }

    "Zuran Spellcaster" in {
      _tripleColors("2 U").sorted ===
        Seq(
          "Black OR Blue OR Green",
          "Black OR Blue OR Red",
          "Black OR Blue OR White",
          "Blue OR Green OR Red",
          "Blue OR Green OR White",
          "Blue OR Red OR White"
        ).sorted
    }

    "Zhou Yu, Chief Commander" in {
      _tripleColors("5 U U").sorted ===
        Seq(
          "Black OR Blue OR Green",
          "Black OR Blue OR Red",
          "Black OR Blue OR White",
          "Blue OR Green OR Red",
          "Blue OR Green OR White",
          "Blue OR Red OR White"
        ).sorted
    }

    "Yore-Tiller Nephilim" in {
      _tripleColors("W U B R") === Nil
    }

    "Arsenal Thresher" in {
      _tripleColors("2 WB U") === Seq("Black OR Blue OR White")
    }

    "Fireball" in {
      _tripleColors("X R") ===
        Seq(
          "Black OR Blue OR Red",
          "Black OR Green OR Red",
          "Black OR Red OR White",
          "Blue OR Green OR Red",
          "Blue OR Red OR White",
          "Green OR Red OR White"
        ).sorted
    }

    "Reaper King" in {
      _tripleColors("2/W 2/U 2/B 2/R 2/G") === Nil
    }

    "Act of Aggression" in {
      _tripleColors("3 RP RP") ===
        Seq(
          "Black OR Blue OR Red",
          "Black OR Green OR Red",
          "Black OR Red OR White",
          "Blue OR Green OR Red",
          "Blue OR Red OR White",
          "Green OR Red OR White"
        ).sorted
    }

    "Decree of Justice" in {
      _tripleColors("X X 2 W W") ===
        Seq(
          "Black OR Blue OR White",
          "Black OR Green OR White",
          "Black OR Red OR White",
          "Blue OR Green OR White",
          "Blue OR Red OR White",
          "Green OR Red OR White"
        ).sorted
    }

    "Emrakul, the Aeons Torn" in {
      _tripleColors("15") === Nil
    }

    "Autochthon Wurm" in {
      _tripleColors("10 G G G W W") ===
        Seq("Black OR Green OR White", "Blue OR Green OR White", "Green OR Red OR White").sorted
    }

    "Transguild Courier" in {
      _tripleColors(Some("4"), None) === Nil
    }

    "Transguild Courier - new" in {
      _tripleColors(Some("4"), Some(Seq("White", "Blue", "Black", "Red", "Green"))).sorted === Nil
    }

    "Kozilek, the Great Distortion" in {
      _tripleColors("8 C C") === Nil
    }

    "Abstruse Interference" in {
      _tripleColors(Some("2 U"), None).sorted ===
        Seq(
          "Black OR Blue OR Green",
          "Black OR Blue OR Red",
          "Black OR Blue OR White",
          "Blue OR Green OR Red",
          "Blue OR Green OR White",
          "Blue OR Red OR White"
        ).sorted
    }

    "Ludevic's Abomination" in {
      _tripleColors(None, Some(Seq("Blue"))).sorted ===
        Seq(
          "Black OR Blue OR Green",
          "Black OR Blue OR Red",
          "Black OR Blue OR White",
          "Blue OR Green OR Red",
          "Blue OR Green OR White",
          "Blue OR Red OR White"
        ).sorted
    }

    "Dralnu, seigneur liche" in {
      _tripleColors(Some("3 U B"), Some(Seq("Blue", "Black"))).sorted ===
        Seq("Black OR Blue OR Green", "Black OR Blue OR Red", "Black OR Blue OR White").sorted
    }
  }

  private def _tripleColors(cc: String): Seq[String] = _tripleColors(Some(cc), None).sorted
}
