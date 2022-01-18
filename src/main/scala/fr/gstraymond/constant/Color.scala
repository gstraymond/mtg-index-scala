package fr.gstraymond.constant

case class Color(
    symbol: String,
    lbl: String,
    colored: Boolean
)

object Color:
  val BLACK     = Color("B", "Black", colored = true)
  val BLUE      = Color("U", "Blue", colored = true)
  val GREEN     = Color("G", "Green", colored = true)
  val RED       = Color("R", "Red", colored = true)
  val WHITE     = Color("W", "White", colored = true)
  val LIFE      = Color("P", "Life", colored = false)
  val X         = Color("X", "X", colored = false)
  val COLORLESS = Color("C", "Colorless", colored = false)

  val UNCOLORED   = "Uncolored"
  val MONOCOLORED = "1 color"

  def MULTICOLORED(colors: Int) = s"$colors colors"

  val GUILD = "Guild"
  val GOLD  = "Gold"

  val ALL_COLORS_SYMBOLS = Seq(
    BLACK,
    BLUE,
    GREEN,
    RED,
    WHITE,
    LIFE,
    X,
    COLORLESS
  )

  val ONLY_COLORED_SYMBOLS: Seq[Color] = ALL_COLORS_SYMBOLS.filter(_.colored)

  val ALL_DUAL_COLORS: Seq[Seq[Color]] = ONLY_COLORED_SYMBOLS.toSet.subsets(2).toSeq.map(_.toSeq.sortBy(_.lbl))

  val ALL_TRIPLE_COLORS: Seq[Seq[Color]] = ONLY_COLORED_SYMBOLS.toSet.subsets(3).toSeq.map(_.toSeq.sortBy(_.lbl))

  val ALL_QUAD_COLORS: Seq[Seq[Color]] = ONLY_COLORED_SYMBOLS.toSet.subsets(4).toSeq.map(_.toSeq.sortBy(_.lbl))

  // Azorius Senate
  val WU = "WU"

  // Orzhov Syndicate
  val WB = "WB"

  // House Dimir
  val BU = "BU"

  // Izzet League
  val UR = "UR"

  // Cult of Rakdos
  val BR = "BR"

  // Golgari Swarm
  val BG = "BG"

  // Gruul Clans
  val RG = "RG"

  // Boros Legion
  val RW = "RW"

  // Selesnya Conclave
  val GW = "GW"

  // Simic Combine
  val GU = "GU"

  val GUILDS = Seq(
    WU,
    WB,
    BU,
    BR,
    RG,
    RW,
    GW,
    GU,
    UR,
    BG
  )

