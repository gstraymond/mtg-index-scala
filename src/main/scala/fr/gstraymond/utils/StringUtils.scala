package fr.gstraymond.utils

import java.text.Normalizer

object StringUtils {
  def normalize(text: String) = {
    Normalizer
      .normalize(text, Normalizer.Form.NFD)
      .replaceAll("[^\\p{ASCII}]", "")
      .toLowerCase
  }
}
