package fr.gstraymond.parser

import fr.gstraymond.model.RawCard
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OracleRawCardConverterSpec extends Specification {

  "OracleRawCardConverter" should {
    "convert A Display of My Dark Power" in {
      val card = Seq(
        "A Display of My Dark Power",
        "Scheme",
        "When you set this scheme in motion, until your next turn, whenever a player taps a land for mana, that player adds one mana to his or her mana pool of any type that land produced.",
        "ARC-C"
      )
      OracleRawCardConverter.convertCard(card) === RawCard(
        Some("A Display of My Dark Power"),
        None,
        Some("Scheme"),
        None,
        Seq("When you set this scheme in motion, until your next turn, whenever a player taps a land for mana, that player adds one mana to his or her mana pool of any type that land produced."),
        Some("ARC-C")
      )
    }
    "convert Ærathi Berserker" in {
      val card = Seq(
        "Ærathi Berserker",
        "2RRR",
        "Creature -- Human Berserker",
        "2/4",
        "Rampage 3 (Whenever this creature becomes blocked, it gets +3/+3 until end of turn for each creature blocking it beyond the first.)",
        "LE-U"
      )
      OracleRawCardConverter.convertCard(card) === RawCard(
        Some("Ærathi Berserker"),
        Some("2RRR"),
        Some("Creature -- Human Berserker"),
        Some("2/4"),
        Seq("Rampage 3 (Whenever this creature becomes blocked, it gets +3/+3 until end of turn for each creature blocking it beyond the first.)"),
        Some("LE-U")
      )
    }
  }
}
