package fr.gstraymond.rules

import fr.gstraymond.rules.parser.RulesParser
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import scala.io.Source
import scala.annotation.nowarn

@nowarn
@RunWith(classOf[JUnitRunner])
class RulesParserSpec extends Specification:

  "Rules parser" should:
    "parse rules" in:
      val lines = Source.fromResource("rules.txt")("CP1252").getLines().toSeq
      val rules = RulesParser.parse("file", lines)
      rules.rules must have length 3888
      rules.rules.take(10).mkString("\n") === ""
