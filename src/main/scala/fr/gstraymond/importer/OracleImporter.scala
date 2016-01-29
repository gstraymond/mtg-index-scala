package fr.gstraymond.importer

import fr.gstraymond.model.RawCard
import fr.gstraymond.parser.{OracleRawCardConverter, OracleRawParser}
import fr.gstraymond.stats.Timing

object OracleImporter {

  def `import`(path: String): Timing[Seq[RawCard]] = {
    Timing("parse")(OracleRawParser.parse(path)).flatMap { parse =>
      Timing("convert")(OracleRawCardConverter.convert(parse))
    }

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
