package fr.gstraymond.parser

import fr.gstraymond.utils.Log

import scala.io.Source

class OracleRawParser extends Log {

  def parse(path: String): Seq[Seq[String]] = {
    val resource = getClass.getResource(path)
    val lines = Source.fromFile(resource.getFile).getLines()

    lines.foldLeft(Seq(Seq.empty[String])) {
      case (acc, "") => Seq.empty +: acc
      case (acc, line) => (acc.head :+ line) +: acc.tail
    }.filter{
      _.nonEmpty
    }.reverse
  }
}
