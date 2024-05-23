package fr.gstraymond.utils

import com.github.plokhotnyuk.jsoniter_scala.core._

import java.io.File
import java.io.PrintWriter
import scala.util.Using

object FileUtils:

  private val mainPath = "/tmp/mtg-search"

  val scrapPath  = s"$mainPath/scrap"
  val outputPath = s"$mainPath/output"

  def storeJson[A](file: File, a: A)(implicit codec: JsonValueCodec[A]): Unit =
    Using.resource(new PrintWriter(file)):
      _.println(writeToString(a, WriterConfig.withIndentionStep(2)))
