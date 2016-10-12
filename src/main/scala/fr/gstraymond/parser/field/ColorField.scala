package fr.gstraymond.parser.field

import fr.gstraymond.constant.Color
import fr.gstraymond.constant.Color._

trait ColorField {

  def _colors(maybeCastingCost: Option[String], hints: Seq[String], additionalColors: Option[Seq[String]]) = {
    // TODO add allied enemy : http://mtgsalvation.gamepedia.com/Dual_lands
    val uncoloredHint = hints.contains("Devoid (This card has no color.)")
    val cc = maybeCastingCost.getOrElse("") ++ additionalColors.map {
      _.flatMap { color =>
        ALL_COLORS_SYMBOLS.find(_.lbl == color).map(_.symbol)
      }.mkString(" ", " ", "")
    }.getOrElse("")

    cc -> uncoloredHint match {
      case (castingCost, false) if castingCost != "" =>
        def find(colors: Seq[Color]) = colors.filter(c => castingCost.contains(c.symbol))

        val colorHint = hints.find(_.contains("color indicator")).getOrElse("")
        val hintSymbols = ONLY_COLORED_SYMBOLS.map(_.lbl).filter(colorHint.contains)

        val colorCount = Math.max(hintSymbols.size, find(ONLY_COLORED_SYMBOLS).size)

        val colorNumber = colorCount match {
          case 0 => Seq(UNCOLORED)
          case 1 => Seq(MONOCOLORED)
          case s => Seq(MULTICOLORED(s), GOLD)
        }

        val guild = if (GUILDS.exists(castingCost.contains)) Seq(GUILD) else Seq.empty

        val symbols = find(ALL_COLORS_SYMBOLS).map(_.lbl)

        colorNumber ++ guild ++ symbols ++ hintSymbols
      case _ => Seq(UNCOLORED)
    }
  }
}
