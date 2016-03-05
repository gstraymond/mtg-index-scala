package fr.gstraymond.parser

import java.io.File

object OracleConverter {

  def convert(file: File) = {
    val parse = OracleRawParser.parse(file)
    OracleRawCardConverter.convert(parse)
  }
}
