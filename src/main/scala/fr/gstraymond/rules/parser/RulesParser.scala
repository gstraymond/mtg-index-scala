package fr.gstraymond.rules.parser

import fr.gstraymond.rules.model.Rule
import fr.gstraymond.rules.model.RuleLink
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.utils.Log

object RulesParser extends Log {

  def parse(filename: String, lines: Seq[String]): Rules = {
    val emptyMap = ((lines.head -> false) +: lines.tail
      .zip(lines)
      .map { case (line, prev) => line -> prev.isEmpty }
      .filter(_._1.nonEmpty)).zipWithIndex.map { case ((_, prevEmpty), index) =>
      index -> prevEmpty
    }.toMap

    val nonEmptyLines = lines.filter(_.nonEmpty).map {
      case "Glossary" => "10. Glossary"
      case "Credits"  => "11. Credits"
      case l          => l
    }
    val titleIndex        = 0
    val introductionIndex = nonEmptyLines.indexOf("Introduction")
    val contentsIndex     = nonEmptyLines.indexOf("Contents")
    val creditsMenuIndex  = nonEmptyLines.indexOf("11. Credits")
    val glossaryIndex     = nonEmptyLines.lastIndexOf("10. Glossary")
    val creditsIndex      = nonEmptyLines.lastIndexOf("11. Credits")

    val glossaryRange = glossaryIndex + 1 until creditsIndex

    def parseLevel(maybeId: Option[String], position: Int): Int =
      maybeId
        .map {
          case id if id.contains(".") && id.exists(_.isLetter) => 4
          case id if id.contains(".")                          => 3
          case id if id.length == 3                            => 2
          case _                                               => 1
        }
        .getOrElse {
          position match {
            case `titleIndex`        => 1
            case `introductionIndex` => 1
            case `contentsIndex`     => 1
            case _                   => 4
          }
        }

    val rules = nonEmptyLines.zipWithIndex.map {
      case (line, index) if line.head.isDigit && !glossaryRange.contains(index) =>
        val split = line.split(" ", 2)
        val id = split.head match {
          case i if i.last == '.' => i.dropRight(1)
          case i                  => i
        }
        val level = parseLevel(Some(id), index)
        if index <= creditsMenuIndex then
          Rule(id = None, text = split(1), links = Seq(RuleLink(id, 0, split(1).length - 1)), level + 1)
        else Rule(id = Some(id), text = split(1), links = Nil, level)
      case (line, index) =>
        if glossaryRange.contains(index) then {
          val level = if emptyMap(index) then 2 else 4
          Rule(id = None, text = line, links = Nil, level)
        } else Rule(id = None, text = line, links = Nil, parseLevel(None, index))
    }

    val ids = rules.flatMap(_.id).filter(_.length > 2).sortBy(-_.length).map(_.r)

    val range = introductionIndex to creditsIndex
    val rules1 = rules.zipWithIndex.map { case (rule, i) =>
      if range.contains(i) && rule.text.exists(_.isDigit) then {
        ids.filter(r => rule.text.contains(r.pattern.toString)).foldLeft(rule) { (acc, id) =>
          val links = id
            .findAllMatchIn(rule.text)
            .map { r => RuleLink(id.pattern.toString, r.start, r.end - 1) }
            .filterNot { link =>
              val linkRange = link.start to link.end
              acc.links.exists { accLink => (accLink.start to accLink.end).containsSlice(linkRange) }
            }
          acc.copy(links = acc.links ++ links)
        }
      } else {
        rule
      }
    }

    Rules(filename, rules1)
  }
}
