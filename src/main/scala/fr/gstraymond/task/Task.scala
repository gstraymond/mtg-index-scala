package fr.gstraymond.task

import java.io.{File, FileInputStream}

import com.github.plokhotnyuk.jsoniter_scala.core.{ReaderConfig, readFromStream}
import fr.gstraymond.model._
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.scraper.HttpClients
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.{FileUtils, Log}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal

trait Task[A] extends Log {

  private val name = getClass.getSimpleName.replace("$", "")

  def main(args: Array[String]): Unit = {

    val timing = Timing(name) {
      val eventualProcess = process.recover {
        case NonFatal(e) => log.error(s"error during $name", e)
      }
      Await.result(eventualProcess, Duration.Inf)
    }

    log.info(s"Task terminated\n${timing.json}")
    HttpClients.shutdown()
  }

  def process: Future[A]

  import fr.gstraymond.model.MTGCardFormat._
  import fr.gstraymond.model.MTGJsonFormats._
  import fr.gstraymond.model.ScrapedFormatFormat._
  import fr.gstraymond.rules.model.RuleFormats._

  protected def storeMTGCards(cards: Seq[MTGCard]) = {
    mkDir(FileUtils.outputPath)
    val file = new File(s"${FileUtils.outputPath}/cards.json")
    FileUtils.storeJson(file, cards.sortBy(_.title))
    cards
  }

  protected def storeFormats(formats: Seq[ScrapedFormat]) = {
    mkDir(FileUtils.scrapPath)
    val file = new File(s"${FileUtils.scrapPath}/formats.json")
    FileUtils.storeJson(file, formats)
    formats
  }

  protected def storeRules(rules: Rules) = {
    mkDir(FileUtils.outputPath)
    val file = new File(s"${FileUtils.outputPath}/rules.json")
    FileUtils.storeJson(file, rules)
    rules
  }

  protected def loadAllSet: Map[String, MTGJsonEdition] =
    readFromStream[Map[String, MTGJsonEdition]](
      new FileInputStream(s"${FileUtils.scrapPath}/AllPrintings.json"),
      ReaderConfig(preferredBufSize = 30 * 1024 * 1024)
    )

  protected def loadFormats: Seq[ScrapedFormat] = {
    val json = new FileInputStream(s"${FileUtils.scrapPath}/formats.json")
    readFromStream[Seq[ScrapedFormat]](json)
  }

  protected def loadMTGCards: Seq[MTGCard] = {
    val json = new FileInputStream(s"${FileUtils.outputPath}/cards.json")
    readFromStream[Seq[MTGCard]](json)
  }

  protected def loadRules: Seq[Rules] = {
    val json = new FileInputStream(s"${FileUtils.outputPath}/rules.json")
    Seq(readFromStream[Rules](json))
  }

  private def mkDir(path: String) = {
    val dir = new File(path)
    if (!dir.exists()) dir.mkdirs()
  }
}