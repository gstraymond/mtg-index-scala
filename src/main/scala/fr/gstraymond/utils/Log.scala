package fr.gstraymond.utils

import org.slf4j.LoggerFactory

trait Log {

  protected def log = LoggerFactory.getLogger(getClass)

}
