package fr.gstraymond.parser.field

import fr.gstraymond.constant.Abilities

trait AbilitiesField {

  def _abilities(`type`: Option[String], description: Seq[String]) =
    Abilities.LIST.filter(description.mkString(" ").contains)
}
