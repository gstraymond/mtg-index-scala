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
        Seq("ARC-C")
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
        Seq("LE-U")
      )
    }

    "convert Loost Lips" in {
      val card = Seq(
        "Loose Lips",
        "U",
        "Enchant Creature",
        "As Loose Lips comes into play, choose a sentence with eight or fewer words.",
        "Enchanted creature has flying.",
        "Whenever enchanted creature deals damage to an opponent, you draw two cards unless that player says the chosen sentence.",
        "UNH-C"
      )

      OracleRawCardConverter.convertCard(card) === RawCard(
        Some("Loose Lips"),
        Some("U"),
        Some("Enchant Creature"),
        None,
        Seq(
          "As Loose Lips comes into play, choose a sentence with eight or fewer words.",
          "Enchanted creature has flying.",
          "Whenever enchanted creature deals damage to an opponent, you draw two cards unless that player says the chosen sentence."),
        Seq("UNH-C")
      )
    }
  }
}
