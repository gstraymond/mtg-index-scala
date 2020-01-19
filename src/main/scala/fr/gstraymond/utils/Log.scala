package fr.gstraymond.utils

import org.slf4j.{Logger, LoggerFactory}

trait Log {

  protected val log: Logger = LoggerFactory.getLogger(getClass.getName.replace("$", ""))

}
