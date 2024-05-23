package fr.gstraymond.task

import com.github.plokhotnyuk.jsoniter_scala.core.ReaderConfig
import com.github.plokhotnyuk.jsoniter_scala.core.readFromStream
import fr.gstraymond.model._
import fr.gstraymond.rules.model.Rules
import fr.gstraymond.scraper.HttpClients
import fr.gstraymond.stats.Timing
import fr.gstraymond.utils.FileUtils
import fr.gstraymond.utils.Log

import java.io.File
import java.io.FileInputStream
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

trait Task[A] extends Log:

  private val name = getClass.getSimpleName.replace("$", "")

  def main(args: Array[String]): Unit =

    val timing = Timing(name):
      val eventualProcess = process.recover[Any] { case NonFatal(e) =>
        log.error(s"error during $name", e)
      }
      Await.result(eventualProcess, Duration.Inf)

    log.info(s"Task terminated\n${timing.json}")
    HttpClients.shutdown()

  def process: Future[A]

  import fr.gstraymond.model.MTGCardFormat._
  import fr.gstraymond.model.MTGJsonFormats._
  import fr.gstraymond.model.ScrapedFormatFormat._
  import fr.gstraymond.rules.model.RuleFormats._
  import fr.gstraymond.parser.PriceModels._

  protected def storeMTGCards(cards: Seq[MTGCard]) =
    mkDir(FileUtils.outputPath)
    val file = new File(s"${FileUtils.outputPath}/cards.json")
    FileUtils.storeJson(file, cards.sortBy(_.title))
    cards

  protected def storeFormats(formats: Seq[ScrapedFormat]) =
    mkDir(FileUtils.scrapPath)
    val file = new File(s"${FileUtils.scrapPath}/formats.json")
    FileUtils.storeJson(file, formats)
    formats

  protected def storeRules(rules: Rules) =
    mkDir(FileUtils.outputPath)
    val file = new File(s"${FileUtils.outputPath}/rules.json")
    FileUtils.storeJson(file, rules)
    rules

  protected def storePrices(prices: Seq[CardPrice]) =
    mkDir(FileUtils.outputPath)
    val file = new File(s"${FileUtils.outputPath}/prices.json")
    FileUtils.storeJson(file, prices)
    prices

  protected def loadAllSet: Map[String, MTGJsonEdition] =
    readFromStream[MTGJsonAllPrintings](
      new FileInputStream(s"${FileUtils.scrapPath}/AllPrintings.json"),
      ReaderConfig.withPreferredBufSize(30 * 1024 * 1024)
    ).data

  protected def loadAllPrices: Seq[CardPrice] =
    readFromStream[Seq[CardPrice]](
      new FileInputStream(s"${FileUtils.outputPath}/prices.json"),
      ReaderConfig.withPreferredBufSize(1 * 1024 * 1024)
    )

  protected def loadFormats: Seq[ScrapedFormat] =
    val json = new FileInputStream(s"${FileUtils.scrapPath}/formats.json")
    readFromStream[Seq[ScrapedFormat]](json)

  protected def loadMTGCards: Seq[MTGCard] =
    val json = new FileInputStream(s"${FileUtils.outputPath}/cards.json")
    readFromStream[Seq[MTGCard]](json)

  protected def loadRules: Seq[Rules] =
    val json = new FileInputStream(s"${FileUtils.outputPath}/rules.json")
    Seq(readFromStream[Rules](json))

  private def mkDir(path: String) =
    val dir = new File(path)
    if !dir.exists() then 
      val _ = dir.mkdirs()
