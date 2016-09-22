package fr.gstraymond.indexer

import dispatch.Defaults._
import dispatch._
import fr.gstraymond.model.MTGCard
import fr.gstraymond.utils.StringUtils
import play.api.libs.json.Json

import scala.concurrent.Future

object EsCardIndexer extends EsIndexer {

  override val index = "magic"
  override val `type` = "card"

  def exists(cards: Seq[MTGCard]): Future[Seq[String]] = Future.sequence {
    cards.map { card =>
      val s = s"$indexPath/${`type`}/${norm(card.title)}-${norm(card.`type`)}"
      Http {
        url(s) > as.String
      }.map { resp =>
        (Json.parse(resp) \ "found").as[Boolean] match {
          case true => None
          case _ =>
            log.info(s"${card.title}")
            Some(card.title)
        }
      }
    }
  }.map(_.flatten).map { r =>
    log.info(s"result: $r")
    r
  }

  override def buildBody(group: Seq[MTGCard]): String = {
    group.flatMap { card =>

      val indexJson = Json.obj("index" -> Json.obj("_id" -> norm(card.title)))

      import fr.gstraymond.model.MTGCardFormat._
      val cardJson = Json.toJson(card)

      Seq(indexJson, cardJson).map(Json.stringify)
    }.mkString("\n") + "\n"
  }

  private def norm(string: String) = StringUtils.normalize(string)
}
