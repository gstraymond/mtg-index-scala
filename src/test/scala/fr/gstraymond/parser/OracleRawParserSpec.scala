package fr.gstraymond.parser

import java.io.File

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.Specification

@RunWith(classOf[JUnitRunner])
class OracleRawParserSpec extends Specification {

  "A test" should {
    "check" in {
      val file = new File(getClass.getResource("/oracleRawParser.txt").getFile)
      val result = OracleRawParser.parse(file)
      result must have size 5
      result.head === Seq(
        "A Display of My Dark Power",
        "Scheme",
        "When you set this scheme in motion, until your next turn, whenever a player taps a land for mana, that player adds one mana to his or her mana pool of any type that land produced.",
        "ARC-C"
      )
    }
  }
}
