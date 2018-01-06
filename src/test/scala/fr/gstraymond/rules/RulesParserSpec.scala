package fr.gstraymond.rules

import fr.gstraymond.rules.parser.RulesParser
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import scala.io.Codec.ISO8859
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class RulesParserSpec extends Specification {

  "Rules parser" should {
    "parse rules" in {
      val lines = Source.fromResource("rules.txt")(ISO8859).getLines().toSeq
      val rules = RulesParser.parse(lines)
      rules.rules must have length 3888
      //rules.take(10).mkString("\n") === ""
    }
  }
}
