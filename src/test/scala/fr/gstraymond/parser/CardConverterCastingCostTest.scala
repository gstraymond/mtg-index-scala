package fr.gstraymond.parser

import fr.gstraymond.model.RawCard
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CardConverterCastingCostTest extends Specification {

  "card converter" should {
    "Nope" in {
      CardConverter._cc(card(None)) === None
    }

    "Zuran Spellcaster" in {
      _cc("2U") === "2 U"
    }

    "Zhou Yu, Chief Commander" in {
      _cc("5UU") === "5 U U"
    }

    "Yore-Tiller Nephilim" in {
      _cc("WUBR") === "W U B R"
    }

    "Arsenal Thresher" in {
      _cc("2(w/b)U") === "2 WB U"
    }

    "Fireball" in {
      _cc("XR") === "X R"
    }

    "Reaper King" in {
      _cc("(2/w)(2/u)(2/b)(2/r)(2/g)") === "2/W 2/U 2/B 2/R 2/G"
    }

    "Act of Aggression" in {
      _cc("3(r/p)(r/p)") === "3 RP RP"
    }

    "Decree of Justice" in {
      _cc("XX2WW") === "X X 2 W W"
    }

    "Emrakul, the Aeons Torn" in {
      _cc("15") === "15"
    }

    "Autochthon Wurm" in {
      _cc("10GGGWW") === "10 G G G W W"
    }
  }

  private def _cc(cc: String) = CardConverter._cc(card(Some(cc))).getOrElse("NO CC")

  private def card(cc: Option[String]) = RawCard(
    title = None,
    castingCost = cc,
    `type` = None,
    powerToughness = None,
    description = Seq.empty,
    editionRarity = Seq.empty
  )
}
