package fr.gstraymond.task

import fr.gstraymond.importer.OracleImporter
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.Log
import play.api.libs.json.Json

object Importer extends Log {

  def main(args: Array[String]): Unit = {
    val timingExpansed = Timing("global") {
      OracleImporter.`import`("/All-Sets-2015-11-15.txt")
    }
    val timing = timingExpansed.flatten(timingExpansed.get)
    log.info(Json.prettyPrint(timing.json))
  }
}