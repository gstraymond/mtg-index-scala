package fr.gstraymond.parser

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class OracleRawParserSpec extends Specification {

  "A test" should {
    "check" in {
      val result = new OracleRawParser().parse("/oracleRawParser.txt")
      result must have size 4
      result.head === Seq(
        "A Display of My Dark Power",
        "Scheme",
        "When you set this scheme in motion, until your next turn, whenever a player taps a land for mana, that player adds one mana to his or her mana pool of any type that land produced.",
        "ARC-C"
      )
    }
  }
}
