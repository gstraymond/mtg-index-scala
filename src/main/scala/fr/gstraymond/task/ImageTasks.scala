package fr.gstraymond.task

import fr.gstraymond.image.ImageConverter

import scala.concurrent.Future

object Gif2PngConvertTask extends Task[Unit]:
  override def process = Future.successful {
    ImageConverter.convert()
  }
