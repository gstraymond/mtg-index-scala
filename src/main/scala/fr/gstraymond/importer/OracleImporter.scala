package fr.gstraymond.importer

import fr.gstraymond.model.RawCard
import fr.gstraymond.parser.{OracleRawCardConverter, OracleRawParser}

object OracleImporter {

  def `import`(path: String): Seq[RawCard] = {
      OracleRawCardConverter.convert(OracleRawParser.parse(path))

    /*
    for {
      parse <- Timing("parse")(OracleRawParser.parse(path))
      convert <- Timing("convert")(OracleRawCardConverter(parse).convert())
    } yield {
      convert
    }
    */
  }
}
