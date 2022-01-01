package fr.gstraymond.utils

import java.text.Normalizer

object StringUtils:
  def normalize(text: String) =
    Normalizer
      .normalize(text.toLowerCase, Normalizer.Form.NFD)
      .replace(" ", "-")
      .replaceAll("[^A-Za-z0-9\\-\\_]", "")
      .replace("\"", "")
      .replace("“", "")
      .replace("”", "")
      .replace("aether", "ther")
      .replace("aerathi", "rathi")
      .replace("coast(r)", "coast")
