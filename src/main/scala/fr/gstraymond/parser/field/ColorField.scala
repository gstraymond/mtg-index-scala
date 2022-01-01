package fr.gstraymond.parser.field

import fr.gstraymond.constant.Color
import fr.gstraymond.constant.Color._

trait ColorField:

  def _colors(
      maybeCastingCost: Option[String],
      hints: Seq[String],
      additionalColors: Option[Seq[String]]
  ): Seq[String] =
    // TODO add allied enemy : http://mtgsalvation.gamepedia.com/Dual_lands
    ccWithHint(maybeCastingCost, additionalColors, hints) match
      case (castingCost, false) if castingCost != "" =>
        val colorHint   = hints.find(_.contains("color indicator")).getOrElse("")
        val hintSymbols = ONLY_COLORED_SYMBOLS.map(_.lbl).filter(colorHint.contains)

        val colorCount = Math.max(hintSymbols.size, find(castingCost)(ONLY_COLORED_SYMBOLS).size)

        val colorNumber = colorCount match
          case 0 => Seq(UNCOLORED)
          case 1 => Seq(MONOCOLORED)
          case s => Seq(MULTICOLORED(s), GOLD)

        val guild = if GUILDS.exists(castingCost.contains) then Seq(GUILD) else Seq.empty

        val symbols = find(castingCost)(ALL_COLORS_SYMBOLS).map(_.lbl)

        colorNumber ++ guild ++ symbols ++ hintSymbols
      case _ => Seq(UNCOLORED)

  def _dualColors(maybeCastingCost: Option[String], additionalColors: Option[Seq[String]]): Seq[String] =
    ccWithHint(maybeCastingCost, additionalColors, Nil) match
      case (castingCost, false) if castingCost != "" =>
        {
          ALL_DUAL_COLORS.filter(find(castingCost)(_).size == 2) match
            case seq if seq.size == 1 => seq
            case seq if seq.size > 1  => Nil
            case _                    => ALL_DUAL_COLORS.filter(find(castingCost)(_).size == 1)
        }.map {
          case Seq(c1, c2) => s"${c1.lbl} OR ${c2.lbl}"
          case any         => throw new RuntimeException(s"should not happen: $any has not length of 2")
        }
      case _ => Nil

  def _tripleColors(maybeCastingCost: Option[String], additionalColors: Option[Seq[String]]): Seq[String] =
    ccWithHint(maybeCastingCost, additionalColors, Nil) match
      case (castingCost, false) if castingCost != "" =>
        {
          ALL_TRIPLE_COLORS.filter(find(castingCost)(_).size == 3) match
            case seq if seq.size == 1 => seq
            case seq if seq.size > 1  => Nil
            case _ =>
              ALL_TRIPLE_COLORS.filter(find(castingCost)(_).size == 2) match
                case seq if seq.size == 3 => seq
                case _                    => ALL_TRIPLE_COLORS.filter(find(castingCost)(_).size == 1)
        }.map {
          case Seq(c1, c2, c3) => s"${c1.lbl} OR ${c2.lbl} OR ${c3.lbl}"
          case any             => throw new RuntimeException(s"should not happen: $any has not length of 3")
        }
      case _ => Nil

  private def ccWithHint(
      maybeCastingCost: Option[String],
      additionalColors: Option[Seq[String]],
      hints: Seq[String]
  ) =
    val uncoloredHint = hints.contains("Devoid (This card has no color.)")
    val cc = maybeCastingCost.getOrElse("") ++ additionalColors
      .map {
        _.flatMap { color =>
          ALL_COLORS_SYMBOLS.find(_.lbl == color).map(_.symbol)
        }.mkString(" ", " ", "")
      }
      .getOrElse("")

    cc -> uncoloredHint

  private def find(castingCost: String)(colors: Seq[Color]) =
    colors.filter(c => castingCost.contains(c.symbol))
