package fr.gstraymond.parser.field

import fr.gstraymond.constant.Color
import fr.gstraymond.constant.Color.*

trait ColorField {

  def _colors(
      maybeCastingCost: Option[String],
      hints: Seq[String],
      additionalColors: Option[Seq[String]]
  ): Seq[String] =
    // TODO add allied enemy : http://mtgsalvation.gamepedia.com/Dual_lands
    ccWithHint(maybeCastingCost, additionalColors, hints) match {
      case (castingCost, false) if castingCost != "" =>
        val colorHint   = hints.find(_.contains("color indicator")).getOrElse("")
        val hintSymbols = ONLY_COLORED_SYMBOLS.map(_.lbl).filter(colorHint.contains)

        val colorCount = Math.max(hintSymbols.size, find(castingCost)(ONLY_COLORED_SYMBOLS).size)

        val colorNumber = colorCount match {
          case 0 => Seq(UNCOLORED)
          case 1 => Seq(MONOCOLORED)
          case s => Seq(MULTICOLORED(s), GOLD)
        }

        val guild = if GUILDS.exists(castingCost.contains) then Seq(GUILD) else Seq.empty

        val symbols = find(castingCost)(ALL_COLORS_SYMBOLS).map(_.lbl)

        colorNumber ++ guild ++ symbols ++ hintSymbols
      case _ => Seq(UNCOLORED)
    }

  def _dualColors(maybeCastingCost: Option[String], additionalColors: Option[Seq[String]]): Seq[String] =
    ccWithHint(maybeCastingCost, additionalColors, Nil) match {
      case (castingCost, false) if castingCost != "" =>
        val colors = splitColors(castingCost)
        ALL_DUAL_COLORS
          .filter { all =>
            val count = all.count(colors)
            colors.size match {
              case 2 => count == 2
              case 1 => count > 0 && count <= 2
              case _ => false
            }
          }
          .map {
            case Seq(c1, c2) => s"${c1.lbl} OR ${c2.lbl}"
            case any         => throw new RuntimeException(s"should not happen: $any has not length of 2")
        }
      case _ => Nil
    }

  def _tripleColors(maybeCastingCost: Option[String], additionalColors: Option[Seq[String]]): Seq[String] =
    ccWithHint(maybeCastingCost, additionalColors, Nil) match {
      case (castingCost, false) if castingCost != "" =>
        val colors = splitColors(castingCost)
        ALL_TRIPLE_COLORS
          .filter { all =>
            val count = all.count(colors)
            colors.size match {
              case 3 => count == 3
              case 2 => count > 1 && count <= 3
              case 1 => count > 0 && count <= 3
              case _ => false
            }
          }
          .map {
            case Seq(c1, c2, c3) => s"${c1.lbl} OR ${c2.lbl} OR ${c3.lbl}"
            case any             => throw new RuntimeException(s"should not happen: $any has not length of 3")
        }
      case _ => Nil
    }

  def _quadColors(maybeCastingCost: Option[String], additionalColors: Option[Seq[String]]): Seq[String] =
    ccWithHint(maybeCastingCost, additionalColors, Nil) match {
      case (castingCost, false) if castingCost != "" =>
        val colors = splitColors(castingCost)
        ALL_QUAD_COLORS
          .filter { all =>
            val count = all.count(colors)
            colors.size match {
              case 4 => count == 4
              case 3 => count > 2 && count <= 4
              case 2 => count > 1 && count <= 4
              case 1 => count > 0 && count <= 4
              case _ => false
            }
          }
          .map {
            case Seq(c1, c2, c3, c4) => s"${c1.lbl} OR ${c2.lbl} OR ${c3.lbl} OR ${c4.lbl}"
            case any                 => throw new RuntimeException(s"should not happen: $any has not length of 3")
        }
      case _ => Nil
    }

  private def ccWithHint(
      maybeCastingCost: Option[String],
      additionalColors: Option[Seq[String]],
      hints: Seq[String]
  ) = {
    val uncoloredHint = hints.contains("Devoid (This card has no color.)")
    val cc = maybeCastingCost.getOrElse("") ++ additionalColors
      .map {
        _.flatMap { color =>
          ALL_COLORS_SYMBOLS.find(_.lbl == color).map(_.symbol)
        }.mkString(" ", " ", "")
    }
      .getOrElse("")

    cc -> uncoloredHint
  }

  private def find(castingCost: String)(colors: Seq[Color]) =
    colors.filter(c => castingCost.contains(c.symbol))

  private def splitColors(castingCost: String): Set[Color] =
    castingCost
      .replace(" ", "")
      .toList
      .flatMap(c => ONLY_COLORED_SYMBOLS.find(_.symbol == c.toString))
      .toSet
}
