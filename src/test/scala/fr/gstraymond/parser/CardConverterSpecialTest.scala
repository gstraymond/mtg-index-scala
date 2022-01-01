package fr.gstraymond.parser

import fr.gstraymond.parser.field.SpecialField
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CardConverterSpecialTest extends Specification:

  "card converter" should {

    "Alaborn Trooper" in {
      _special(
        "Alaborn Trooper",
        "Creature — Human Soldier",
        ""
      ) === Seq("Vanilla")
    }

    "Mox Jet" in {
      _special(
        "Mox Jet",
        "Artifact",
        "{T}: Add {B} to your mana pool."
      ) === Seq("Power 9")
    }

    "Jötun Grunt" in {
      _special(
        "Jötun Grunt",
        "Creature — Giant Soldier",
        "Cumulative upkeep—Put two cards from a single graveyard on the bottom of their owner's library. (At the beginning of your upkeep, put an age counter on this permanent, then sacrifice it unless you pay its upkeep cost for each age counter on it.)"
      ) === Seq("Tuck")
    }

    "Infernal Tutor" in {
      _special(
        "Infernal Tutor",
        "Sorcery",
        "Reveal a card from your hand. Search your library for a card with the same name as that card, reveal it, put it into your hand, then shuffle your library.",
        "Hellbent — If you have no cards in hand, instead search your library for a card, put it into your hand, then shuffle your library."
      ) === Seq("Tutor")
    }

    "Burning Wish" in {
      _special(
        "Burning Wish",
        "Sorcery",
        "You may choose a sorcery card you own from outside the game, reveal that card, and put it into your hand. Exile Burning Wish."
      ) === Seq("Tutor")
    }

    "Abrupt Decay" in {
      _special(
        "Abrupt Decay",
        "Instant",
        "Abrupt Decay can't be countered by spells or abilities.",
        "Destroy target nonland permanent with converted mana cost 3 or less."
      ) === Seq("Uncounterable")
    }

    "Plague Fiend" in {
      _special(
        "Plague Fiend",
        "Creature — Insect",
        "Whenever Plague Fiend deals combat damage to a creature, destroy that creature unless its controller pays {2}."
      ) === Seq("Venom")
    }

    "Abomination" in {
      _special(
        "Abomination",
        "Creature — Horror",
        "Whenever Abomination blocks or becomes blocked by a green or white creature, destroy that creature at end of combat."
      ) === Seq("Venom")
    }

    "Cryoclasm" in {
      _special(
        "Cryoclasm",
        "Sorcery",
        "Destroy target Plains or Island. Cryoclasm deals 3 damage to that land's controller."
      ) === Seq("Land Destruction")
    }

    "Wasteland" in {
      _special(
        "Wasteland",
        "Land",
        "{T}: Add {C} to your mana pool.",
        "{T}, Sacrifice Wasteland: Destroy target nonbasic land."
      ) === Seq("Land Destruction")
    }

    "Blessed Orator" in {
      _special(
        "Blessed Orator",
        "Creature — Human Cleric",
        "Other creatures you control get +0/+1."
      ) === Seq.empty
    }

    "Zombie Master" in {
      _special(
        "Zombie Master",
        "Creature — Zombie",
        "Other Zombie creatures have swampwalk. (They can't be blocked as long as defending player controls a Swamp.)",
        "Other Zombies have \"{B}: Regenerate this permanent.\""
      ) === Seq("Lord")
    }

    "Karona, False God" in {
      _special(
        "Karona, False God",
        "Legendary Creature — Avatar",
        "Haste",
        "At the beginning of each player's upkeep, that player untaps Karona, False God and gains control of it.",
        "Whenever Karona attacks, creatures of the creature type of your choice get +3/+3 until end of turn."
      ) === Seq.empty
    }

    "Urza's Incubator" in {
      _special(
        "Urza's Incubator",
        "Artifact",
        "As Urza's Incubator enters the battlefield, choose a creature type.",
        "Creature spells of the chosen type cost {2} less to cast."
      ) === Seq.empty
    }

    "Goblin Lookout" in {
      _special(
        "Goblin Lookout",
        "Creature — Goblin",
        "{T}, Sacrifice a Goblin: Goblin creatures get +2/+0 until end of turn."
      ) === Seq.empty
    }

    "Stinkdrinker Daredevil" in {
      _special(
        "Stinkdrinker Daredevil",
        "Creature — Goblin Rogue",
        "Giant spells you cast cost {2} less to cast."
      ) === Seq.empty
    }

    "Catastrophe" in {
      _special(
        "Catastrophe",
        "Sorcery",
        "Destroy all lands or all creatures. Creatures destroyed this way can't be regenerated."
      ) === Seq("Wrath Effect")
    }

    "Inundate" in {
      _special(
        "Inundate",
        "Sorcery",
        "Return all nonblue creatures to their owners' hands."
      ) === Seq("Wrath Effect")
    }

    "Archive Trap" in {
      _special(
        "Archive Trap",
        "Instant — Trap",
        "If an opponent searched his or her library this turn, you may pay {0} rather than pay Archive Trap's mana cost.",
        "Target opponent puts the top thirteen cards of his or her library into his or her graveyard."
      ) === Seq("Mill")
    }

    "Ashiok, Nightmare Weaver" in {
      _special(
        "Ashiok, Nightmare Weaver",
        "Planeswalker — Ashiok",
        "+2: Exile the top three cards of target opponent's library.",
        "−X: Put a creature card with converted mana cost X exiled with Ashiok, Nightmare Weaver onto the battlefield under your control. That creature is a Nightmare in addition to its other types.",
        "−10: Exile all cards from all opponents' hands and graveyards."
      ) === Seq("Mill")
    }
  }

  private def _special(title: String, `type`: String, description: String*) =
    new SpecialField {}._special(title, `type`, description)
