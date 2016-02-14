package fr.gstraymond.scraper

import java.text.SimpleDateFormat
import java.util.Date

import fr.gstraymond.model.ScrapedEdition
import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ReleaseDateScraper extends MTGPriceScraper {

  val path = "/magic-the-gathering-prices.jsp"

  val dateFormat = new SimpleDateFormat("MM/dd/yyyy")

  def scrap(editions: Seq[ScrapedEdition]): Future[Seq[ScrapedEdition]] = {
    get(path).map { doc =>
      log.info(s"scraping doc ${doc.title()}")
      extractReleaseDate(doc)
    }.map { result =>
      merge(editions, result)
    }
  }

  private def extractReleaseDate(doc: Document) = {
    doc.select("#setTable tr").asScala.tail.map { tr =>
      tr.select("td").asScala match {
        case Seq(name, releaseDate) =>
          //log.debug(s"extractReleaseDate name $name releaseDAte $releaseDate")
          val scrapedReleaseDate = dateFormat.parse(releaseDate.text())
          normalizeScrap(name.text()) -> scrapedReleaseDate
      }
    }
  }.toMap
    .filterKeys(!_.contains("(foil)"))
    .filterKeys(!Seq("sdcc 2013", "sealed booster boxes").contains(_))

  private def scrapToEdition = Map(
    "10th" -> "tenth",
    "4th" -> "fourth",
    "5th" -> "fifth",
    "6th" -> "classic sixth",
    "7th" -> "seventh",
    "8th" -> "eighth",
    "9th" -> "ninth",
    "m10" -> "magic 2010",
    "m11" -> "magic 2011",
    "m12" -> "magic 2012",
    "m13" -> "magic 2013",
    "m14" -> "magic 2014 core set",
    "m15" -> "magic 2015",
    "gateway" -> "wpn/gateway",
    "clash packs" -> "clash pack",
    "duel decks elspeth vs kiora" -> "duel decks kiora vs elspeth",
    "euro land program" -> "european land program",
    "game day" -> "magic game day cards",
    "launch parties" -> "magic the gathering launch parties",
    "planechase 2012 planes" -> "planechase 2012",
    "planechase planes" -> "planechase",
    "player rewards" -> "magic player rewards",
    "ravnica" -> "ravnica city of guilds",
    //"sdcc 2013" -> "",
    //"sealed booster boxes" -> "",
    "timespiral timeshifted" -> "time spiral \"timeshifted\"",
    "world magic cup qualifier" -> "world magic cup qualifiers"
  )

  private def normalizeScrap(editionName: String) = {
    val name = editionName
      //.replace(" Planes", "")
      .replace(" Edition", "")
      .replace(" Schemes", "")
      .replace("_", " ")
      .replace(" Box Set", "")
      .toLowerCase
    scrapToEdition.getOrElse(name, name)
  }

  private def normalizeEdition(editionName: String) =
    editionName
      .replace("'", "")
      .replace(":", "")
      .replace(".", "")
      .replace("Limited Edition ", "")
      .replace(" Edition", "")
      .replace(" Tournament", "")
      .replace(" Box Set", "")
      .toLowerCase

  private def merge(editions: Seq[ScrapedEdition], result: Map[String, Date]) = {
    val mutableMap = mutable.Map(result.toSeq: _*)
    val scrapedEditions = editions.map { edition =>
      val editionName = normalizeEdition(edition.name)
      result.get(editionName) match {
        case Some(releaseDate) =>
          mutableMap.remove(editionName)
          edition.copy(releaseDate = Some(releaseDate))
        case _ => edition
      }
    }

    if (mutableMap.nonEmpty) log.warn(s"missing release dates: ${mutableMap.size}:\n${mutableMap.keys.toSeq.sorted.mkString("\n")}")

    scrapedEditions
  }
}
