package fr.gstraymond.rules.parser

import fr.gstraymond.rules.model.{Rule, Rules}

object RulesParser {

  private val linkCharacters = Set('<', '[', ']', '>')

  def parse(lines: Seq[String]): Rules = {
    val nonEmptyLines = lines.filter(_.nonEmpty)
    val glossaryIndex = nonEmptyLines.indexOf("Glossary")
    val rules = nonEmptyLines.zipWithIndex.map {
      case (line, index) if line.head.isDigit =>
        val split = line.split(" ", 2)
        val id = split.head match {
          case i if i.last == '.' => i.dropRight(1)
          case i => i
        }
        val level = parseLevel(Some(id))
        if (index < glossaryIndex) Rule(id = None, text = split(1), link = Some(id), level)
        else Rule(id = Some(id), text = split(1), link = None, level)
      case (line, _) =>
        Rule(id = None, text = line, link = None, parseLevel(None))
    }

    val ids = rules.flatMap(_.id).filter(_.length > 2).sortBy(-_.length)

    Rules {
      rules.zipWithIndex.map { case (rule, i) =>
        val text = ids.foldLeft(rule.text) { (acc, id) =>
          if (acc.contains(s"<[$id") || i < 5 || i > rules.size - 5) acc
          else {
            val replaced = acc.replace(id, s"<[$id]>")
            replaced.filter(linkCharacters) match {
              case s if s.contains("<[<[") => acc
              case _ => replaced
            }
          }
        }
        rule.copy(text = text)
      }.foldLeft(Seq[Rule]()) { (acc, rule) =>
        acc :+ (rule match {
          case _ if rule.text.startsWith("Example:") =>
            rule.copy(level = acc.last.level)
          case _ => rule
        })
      }
    }
  }

  private def parseLevel(maybeId: Option[String]): Int =
    maybeId.fold(1) {
      case id if id.contains(".") && id.exists(_.isLetter) => 4
      case id if id.contains(".") => 3
      case id if id.length == 3 => 2
      case _ => 1
    }
}
