package fr.gstraymond.utils

import java.text.Normalizer

object StringUtils {
  def normalize(text: String) = {
    Normalizer
      .normalize(text.toLowerCase, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .replace("\"", "")
      .replace("“", "")
      .replace("”", "")
      .replace("aether", "ther")
      .replace("aerathi", "rathi")
  }
}
