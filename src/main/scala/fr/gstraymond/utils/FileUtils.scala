package fr.gstraymond.utils

import com.github.plokhotnyuk.jsoniter_scala.core._

object FileUtils {

  val langs = Seq("en", "fr")

  val mainPath = "src/main/resources"

  val scrapPath = s"$mainPath/scrap"
  val outputPath = s"$mainPath/output"

  def storeJson[A](file: java.io.File,
                   a: A)
                  (implicit codec: JsonValueCodec[A]){
    val writer = new java.io.PrintWriter(file)
    try {
      writer.println(writeToString(a, WriterConfig(indentionStep = 2)))
    } finally {
      writer.close()
    }
  }
}
