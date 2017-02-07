package fr.gstraymond.constant

import java.io.{File, FileInputStream}
import java.util.Properties

object Conf {

  val props: Properties = {
    val properties = new Properties()
    Option(getClass.getResource("/conf.properties")).foreach { resource =>
      properties.load(new FileInputStream(new File(resource.getFile)))
    }
    properties
  }

  def coloredLogs: Boolean =
    Option(props.getProperty("coloredLogs"))
      .exists(_.toBoolean)
}
