package fr.gstraymond.constant

object Land {

  val ALL = Map(
    "Forest"   -> "G",
    "Plains"   -> "W",
    "Mountain" -> "R",
    "Swamp"    -> "B",
    "Island"   -> "U"
  )

  val REV = ALL.map(_.swap)
}
