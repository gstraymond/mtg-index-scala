package fr.gstraymond.indexer

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import scala.annotation.nowarn

object IndexerModels {

  case class Autocomplete(
      suggest: Suggest,
      colors: Option[Seq[String]] = None,
      `type`: Option[String] = None,
      land: Option[Seq[String]] = None,
      stdEditionCode: Option[String] = None
  )

  case class Suggest(input: String, weight: Option[Int] = None)

  implicit val AutocompleteCodec: JsonValueCodec[Autocomplete] =
    JsonCodecMaker.make[Autocomplete](CodecMakerConfig.withTransientEmpty(false))

  case class Index(index: IndexId)

  case class IndexId(_id: String)

  @nowarn
  implicit val IndexCodec: JsonValueCodec[Index] =
    JsonCodecMaker.make[Index](CodecMakerConfig.withTransientEmpty(false))

  case class RuleVersion(filename: String)

  @nowarn
  implicit val RuleVersionCodec: JsonValueCodec[RuleVersion] =
    JsonCodecMaker.make[RuleVersion](CodecMakerConfig.withTransientEmpty(false))
}
