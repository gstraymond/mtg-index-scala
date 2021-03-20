package fr.gstraymond.utils

import java.io._
import java.util.zip.ZipInputStream

import scala.util.Using

object ZipUtils {
  val BUFSIZE = 4096
  val buffer  = new Array[Byte](BUFSIZE)

  def unZip(bytes: Array[Byte], targetFolder: String): Unit =
    Using.resource(new ZipInputStream(new ByteArrayInputStream(bytes))) { zis =>
      LazyList
        .continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .foreach { entry =>
          {
            Using.resource(new FileOutputStream(new File(targetFolder, entry.getName.split("/").last))) { fos =>
              LazyList
                .continually(zis.read(buffer))
                .takeWhile(_ != -1)
                .foreach(fos.write(buffer, 0, _))
            }
          }
        }
    }
}
