package fr.gstraymond.constant

import java.io.{File, FileInputStream}
import java.util.Properties

object Conf {

  val props = {
    val properties = new Properties()
    Option(getClass.getResource("/conf.properties")).foreach { resource =>
      properties.load(new FileInputStream(new File(resource.getFile)))
    }
    properties
  }

  def pictureLocation =
    Option(props.getProperty("pictureLocation"))
      .getOrElse("/root/Dropbox")

  def coloredLogs =
    Option(props.getProperty("coloredLogs"))
      .exists(_.toBoolean)
}
