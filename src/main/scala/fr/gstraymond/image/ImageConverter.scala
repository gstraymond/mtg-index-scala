package fr.gstraymond.image

import fr.gstraymond.constant.URIs
import fr.gstraymond.utils.Log

import java.io.File
import scala.sys.process._

object ImageConverter extends Log {

  val dest = "/Users/guillaume/git/magic-card-search/app/src/main/assets/sets"

  def convert() = {
    val file1: File = new File(URIs.pictureLocation + "/sets")
    log.debug(s"file: ${file1.getAbsolutePath}")
    log.debug(s"file: ${file1.exists()}")
    file1
      .listFiles()
      .toSeq
      .filter(_.isDirectory)
      .foreach { dir =>
        dir
          .listFiles()
          .toSeq
          .filter(_.isFile)
          .foreach { file =>
            s"file ${file.getAbsolutePath}".!! match {
              case result if result.contains("PNG image data") => copyPng(file)
              case result if result.contains("GIF image data") => convertGif(file)
            }
          }
      }
  }

  private def copyPng(file: File): Unit = {
    val fileName       = file.getName.replace(".gif", ".png")
    val parentName     = file.getParentFile.getName
    val destParentPath = s"$dest/$parentName"
    new File(destParentPath) match {
      case f if !f.exists() => f.mkdirs()
      case _                =>
    }
    execute(s"cp ${file.getAbsolutePath} $destParentPath/$fileName")
  }

  private def convertGif(file: File): Unit = {
    val parentName     = file.getParentFile.getName
    val destParentPath = s"$dest/$parentName"
    new File(destParentPath) match {
      case f if !f.exists() => f.mkdirs()
      case _                =>
    }

    execute(s"cp ${file.getAbsolutePath} $destParentPath/${file.getName}")
    execute(s"gif2png -Od $destParentPath/${file.getName}")
  }

  private def execute(command: String): Unit =
    command.! match {
      case 1 => log.error(s"error during command $command")
      case _ =>
    }
}
