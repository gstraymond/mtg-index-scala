package fr.gstraymond.parser

import fr.gstraymond.parser.field.ColorField
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scala.annotation.nowarn

@nowarn
@RunWith(classOf[JUnitRunner])
class CardConverterQuadColorsTest extends Specification with ColorField:

  "card converter" should:
    "Nope" in:
      _quadColors(None, None) === Nil

    "Zuran Spellcaster" in:
      _quadColors("2 U").sorted ===
        Seq(
          "Black OR Blue OR Green OR Red",
          "Black OR Blue OR Green OR White",
          "Black OR Blue OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Zhou Yu, Chief Commander" in:
      _quadColors("5 U U").sorted ===
        Seq(
          "Black OR Blue OR Green OR Red",
          "Black OR Blue OR Green OR White",
          "Black OR Blue OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Yore-Tiller Nephilim" in:
      _quadColors("W U B R") === Seq(
        "Black OR Blue OR Red OR White"
      )

    "Arsenal Thresher" in:
      _quadColors("2 WB U") === Seq(
        "Black OR Blue OR Green OR White",
        "Black OR Blue OR Red OR White"
      )

    "Fireball" in:
      _quadColors("X R") ===
        Seq(
          "Black OR Blue OR Green OR Red",
          "Black OR Blue OR Red OR White",
          "Black OR Green OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Reaper King" in:
      _quadColors("2/W 2/U 2/B 2/R 2/G") === Nil

    "Act of Aggression" in:
      _quadColors("3 RP RP") ===
        Seq(
          "Black OR Blue OR Green OR Red",
          "Black OR Blue OR Red OR White",
          "Black OR Green OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Decree of Justice" in:
      _quadColors("X X 2 W W") ===
        Seq(
          "Black OR Blue OR Green OR White",
          "Black OR Blue OR Red OR White",
          "Black OR Green OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Emrakul, the Aeons Torn" in:
      _quadColors("15") === Nil

    "Autochthon Wurm" in:
      _quadColors("10 G G G W W") ===
        Seq(
          "Black OR Blue OR Green OR White",
          "Black OR Green OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Transguild Courier" in:
      _quadColors(Some("4"), None) === Nil

    "Transguild Courier - new" in:
      _quadColors(Some("4"), Some(Seq("White", "Blue", "Black", "Red", "Green"))).sorted === Nil

    "Kozilek, the Great Distortion" in:
      _quadColors("8 C C") === Nil

    "Abstruse Interference" in:
      _quadColors(Some("2 U"), None).sorted ===
        Seq(
          "Black OR Blue OR Green OR Red",
          "Black OR Blue OR Green OR White",
          "Black OR Blue OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Ludevic's Abomination" in:
      _quadColors(None, Some(Seq("Blue"))).sorted ===
        Seq(
          "Black OR Blue OR Green OR Red",
          "Black OR Blue OR Green OR White",
          "Black OR Blue OR Red OR White",
          "Blue OR Green OR Red OR White"
        ).sorted

    "Dralnu, seigneur liche" in:
      _quadColors(Some("3 U B"), Some(Seq("Blue", "Black"))).sorted ===
        Seq(
          "Black OR Blue OR Green OR Red",
          "Black OR Blue OR Green OR White",
          "Black OR Blue OR Red OR White"
        ).sorted

  private def _quadColors(cc: String): Seq[String] = _quadColors(Some(cc), None).sorted
