import fr.gstraymond.parser.OracleRawParser
import fr.gstraymond.utils.Log

object Test extends Log {

  def main(args: Array[String]): Unit = {
    log.info("Hello, world!")
    new OracleRawParser().parse("/test.txt")
  }
}