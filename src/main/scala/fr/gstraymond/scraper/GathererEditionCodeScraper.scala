package fr.gstraymond.scraper

import java.net.URLEncoder

import fr.gstraymond.model.ScrapedEdition

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GathererEditionCodeScraper extends GathererScraper {

  val path = "/Pages/Search/Default.aspx?set="

  val editionCodeMapping = Map(
    "Magic 2015" -> "Magic 2015 Core Set",
    "MTGO Masters Edition" -> "Masters Edition",
    "MTGO Masters Edition II" -> "Masters Edition II",
    "MTGO Masters Edition III" -> "Masters Edition III",
    "MTGO Masters Edition IV" -> "Masters Edition IV",
    "Commander" -> "Commander 2013 Edition",
    "Commander 2014 Edition" -> "Commander 2014",
    "Ugin's Fate" -> "Ugin's Fate promos",
    "Duel Decks Anthology: Jace vs. Chandra" -> "Duel Decks Anthology, Jace vs. Chandra",
    "Duel Decks Anthology: Garruk vs. Liliana" -> "Duel Decks Anthology, Garruk vs. Liliana",
    "Duel Decks Anthology: Elves vs. Goblins" -> "Duel Decks Anthology, Elves vs. Goblins",
    "Duel Decks Anthology: Divine vs. Demonic" -> "Duel Decks Anthology, Divine vs. Demonic",
    "Duel Decks: Kiora vs. Elspeth" -> "Duel Decks: Elspeth vs. Kiora"
  )

  def scrap(editions: Seq[ScrapedEdition], cache: Map[String, String]): Future[(Seq[ScrapedEdition], Map[String, String])] = {
    Future.sequence {
      editions.map { edition =>
        getCode(edition, cache).map { maybeCode =>
          val code = maybeCode.getOrElse(edition.code)
          edition.copy(stdEditionCode = Some(code)) -> maybeCode
        }
      }
    }.map { editionsWithCode =>
      val editions = editionsWithCode.map(_._1)
      val codes = editionsWithCode.flatMap { case (edition, maybeCode) =>
        maybeCode.map { code =>
          edition.code -> code
        }
      }.toMap

      editions -> codes
    }
  }

  private def getCode(edition: ScrapedEdition, cache: Map[String, String]): Future[Option[String]] = {
    cache.get(edition.code) match {
      case some: Some[_] => Future.successful(some)
      case _ => scrap(buildUrl(edition)).map { doc =>
        val r = doc.select("table.cardItemTable td.setVersions img").asScala.headOption.map { img =>
          val code = img.attr("src").split("set=")(1).split("&").head
          code
        }
        if (r.isEmpty) log.info(s"Not found : ${edition.name} --> ${editionCodeMapping.get(edition.name)}")
        r
      }
    }
  }

  private def buildUrl(edition: ScrapedEdition): String = {
    val name = editionCodeMapping.getOrElse(edition.name, edition.name)
    path + URLEncoder.encode(s"""["$name"]""", "UTF-8")
  }
}
