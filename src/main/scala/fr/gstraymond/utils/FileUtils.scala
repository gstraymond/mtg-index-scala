package fr.gstraymond.utils

import play.api.libs.json.{JsValue, Json}

object FileUtils {

  val langs = Seq("en", "fr")

  val mainPath = "src/main/resources"

  val scrapPath = s"$mainPath/scrap"
  val outputPath = s"$mainPath/output"

  def storeJson(file: java.io.File, json: JsValue) {
    val writer = new java.io.PrintWriter(file)
    try {
      writer.println(Json.prettyPrint(json))
    } finally {
      writer.close()
    }
  }
}
