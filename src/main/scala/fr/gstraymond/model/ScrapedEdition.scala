package fr.gstraymond.model

import java.util.Date

import play.api.libs.json.Json

case class ScrapedEdition(
  code: String,
  name: String,
  releaseDate: Option[Date]
)

object ScrapedEditionFormat {
  implicit val scrapedEditionFormat = Json.format[ScrapedEdition]
}
