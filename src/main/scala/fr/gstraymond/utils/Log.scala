package fr.gstraymond.utils

import org.slf4j.LoggerFactory

trait Log {

  protected val log = LoggerFactory.getLogger(getClass.getName.replace("$", ""))

}
