package fr.gstraymond.utils

import play.api.libs.json.{JsValue, Json}

/**
  * Created by guillaume on 07/02/16.
  */
object FileUtils {

  val langs = Seq("en", "fr")
  val scrapPath = "src/main/resources/scrap"
  val oraclePath = "src/main/resources/oracle"
  val outputPath = "src/main/resources/output"

  def storeJson(file: java.io.File, json: JsValue) {
    val writer = new java.io.PrintWriter(file)
    try {
      writer.println(Json.prettyPrint(json))
    } finally {
      writer.close()
    }
  }
}
