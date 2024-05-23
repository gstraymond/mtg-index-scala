package fr.gstraymond.parser

import fr.gstraymond.parser.field.DevotionsField
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import scala.annotation.nowarn

@nowarn
@RunWith(classOf[JUnitRunner])
class CardConverterDevotionTest extends Specification with DevotionsField:

  "card converter" should:
    "Nope" in:
      _devotions(Some("Instant"), None) === Seq.empty

    "Eye of Nowhere" in:
      _devotions(Some("Sorcery -- Arcane"), Some("U U")) === Seq.empty

    "Zuran Spellcaster" in:
      _devotion("2 U") === Seq(1)

    "Zhou Yu, Chief Commander" in:
      _devotion("5 U U") === Seq(2)

    "Yore-Tiller Nephilim" in:
      _devotion("W U B R") === Seq(1)

    "Arsenal Thresher" in:
      _devotion("2 WB U") === Seq(1)

    "Fireball" in:
      _devotion("X R") === Seq(1)

    "Reaper King" in:
      _devotion("2/W 2/U 2/B 2/R 2/G") === Seq(2)

    "Act of Aggression" in:
      _devotion("3 RP RP") === Seq(2)

    "Decree of Justice" in:
      _devotion("X X 2 W W") === Seq(2)

    "Emrakul, the Aeons Torn" in:
      _devotion("15") === Seq.empty

    "Autochthon Wurm" in:
      _devotion("10 G G G W W") === Seq(2, 3).sorted

    "Kozilek, the Great Distortion" in:
      _devotion("8 C C") === Seq.empty

  private def _devotion(cc: String) =
    _devotions(Some("Creature"), Some(cc)).sorted
