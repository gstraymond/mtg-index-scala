package fr.gstraymond.parser

import fr.gstraymond.utils.Log

import scala.io.{Codec, Source}

object OracleRawParser extends Log {

  def parse(path: String): Seq[Seq[String]] = {
    log.debug(s"path: $path")
    val resource = getClass.getResource(path)
    log.debug(s"resource: $resource")
    val lines = Source.fromFile(resource.getFile)(Codec.ISO8859).getLines()

    lines.zipWithIndex.map { case (line, i) =>
      log.debug(s"$i -> $line")
      line
    }.foldLeft(Seq(Seq.empty[String])) {
      case (acc, "") => Seq.empty +: acc
      case (acc, line) => (acc.head :+ line) +: acc.tail
    }.filter {
      _.nonEmpty
    }.reverse
  }
}
