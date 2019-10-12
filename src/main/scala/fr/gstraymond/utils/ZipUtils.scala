package fr.gstraymond.utils

import java.io._
import java.util.zip.ZipInputStream

object ZipUtils {
  val BUFSIZE = 4096
  val buffer = new Array[Byte](BUFSIZE)

  def unZip(bytes: Array[Byte], targetFolder: String): Unit = {
    val zis = new ZipInputStream(new ByteArrayInputStream(bytes))

    LazyList.continually(zis.getNextEntry)
      .takeWhile(_ != null)
      .foreach { entry => {
        val fos = new FileOutputStream(new File(targetFolder, entry.getName.split("/").last))

        LazyList.continually(zis.read(buffer))
          .takeWhile(_ != -1)
          .foreach(fos.write(buffer, 0, _))

        fos.close()
      }
    }

    zis.close()
  }
}
