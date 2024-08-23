package fr.gstraymond.parser.field

import fr.gstraymond.constant.Color._

trait DevotionsField {

  def _devotions(`type`: Option[String], maybeCastingCost: Option[String]) = {
    val isPermanent = Seq("Instant", "Sorcery").forall { t =>
      `type`.exists {
        !_.contains(t)
      }
    }
    isPermanent -> maybeCastingCost match {
      case (true, Some(castingCost)) =>
        ONLY_COLORED_SYMBOLS
          .map { color =>
            castingCost
              .split(" ")
              .collect {
                case symbol if symbol.contains(color.symbol) && symbol.contains("/") => symbol.head.toString.toInt
                case symbol if symbol.contains(color.symbol)                         => 1
            }
              .sum
          }
          .distinct
          .filter(_ > 0)
      case _ => Seq.empty
    }
  }
}
