package fr.gstraymond.parser

import java.io.File

import fr.gstraymond.utils.Log

import scala.io.{Codec, Source}

object OracleRawParser extends Log {

  def parse(file: File): Seq[Seq[String]] = {
    log.debug(s"path: ${file.getAbsolutePath}")
    val lines = Source.fromFile(file)(Codec.ISO8859).getLines()

    lines.foldLeft(Seq(Seq.empty[String])) {
      case (acc, "") => Seq.empty +: acc
      case (acc, line) => (acc.head :+ line) +: acc.tail
    }.filter {
      _.nonEmpty
    }.reverse
  }
}
