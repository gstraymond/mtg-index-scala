package fr.gstraymond.parser.field

trait AbilitiesField {

  def _abilities(title: String, description: Seq[String], abilities: Seq[String]) =
    abilities.filter { ability =>
      description
        .mkString(" ")
        .replace(title, "")
        .toLowerCase
        .split(" ")
        .flatMap(_.split("\n"))
        .contains(ability.toLowerCase)
    }
}
