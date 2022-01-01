package fr.gstraymond.parser.field

trait HiddenHintsField:

  def _hiddenHints(description: Seq[String]) =
    description.collect {
      case desc @ "Devoid (This card has no color.)" => Seq(desc)
      case desc if desc.contains("[") && desc.contains("]") =>
        desc.split("\\[")(1).split("\\]").head.split("\\.").toSeq
    }.flatten
