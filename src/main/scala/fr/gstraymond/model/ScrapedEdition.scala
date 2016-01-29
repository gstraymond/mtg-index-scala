package fr.gstraymond.model

import play.api.libs.json.Json

case class ScrapedEdition(
  code: String,
  name: String
)

object ScrapedEditionFormat {
  implicit val scrapedEditionFormat = Json.format[ScrapedEdition]
}
