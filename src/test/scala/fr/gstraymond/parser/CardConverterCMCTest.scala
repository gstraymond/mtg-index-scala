package fr.gstraymond.parser

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CardConverterCMCTest extends Specification {

  "card converter" should {
    "Nope" in {
      CardConverter._cmc(None) === 0
    }

    "Zuran Spellcaster" in {
      _cmc("2 U") === 3
    }

    "Zhou Yu, Chief Commander" in {
      _cmc("5 U U") === 7
    }

    "Yore-Tiller Nephilim" in {
      _cmc("W U B R") === 4
    }

    "Arsenal Thresher" in {
      _cmc("2 WB U") === 4
    }

    "Fireball" in {
      _cmc("X R") === 1
    }

    "Reaper King" in {
      _cmc("2/W 2/U 2/B 2/R 2/G") === 10
    }

    "Act of Aggression" in {
      _cmc("3 RP RP") === 5
    }

    "Decree of Justice" in {
      _cmc("X X 2 W W") === 4
    }

    "Emrakul, the Aeons Torn" in {
      _cmc("15") === 15
    }

    "Autochthon Wurm" in {
      _cmc("10 G G G W W") === 15
    }

    "Kozilek, the Great Distortion" in {
      _cmc("8 C C") === 10
    }
  }

  private def _cmc(cc: String) = CardConverter._cmc(Some(cc))
}
