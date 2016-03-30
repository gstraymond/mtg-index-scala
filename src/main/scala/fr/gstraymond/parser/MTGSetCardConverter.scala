package fr.gstraymond.parser

import fr.gstraymond.model.{MTGCard, MTGSetCard}
import fr.gstraymond.utils.Log

object MTGSetCardConverter extends Log {

  def convert(mtgCards: Seq[MTGCard]): Map[String, Seq[MTGSetCard]] = {
    mtgCards.flatMap { mtgCard =>
      mtgCard.publications.map {
        _.editionCode -> mtgCard
      }
    }.groupBy(_._1)
      .mapValues { group =>
        group.map { case (editionCode, mtgCard) =>
          val publication = mtgCard.publications.find(_.editionCode == editionCode).getOrElse {
            throw new RuntimeException(s"publication not found for ${mtgCard.title} and $editionCode")
          }
          MTGSetCard(
            title = mtgCard.title,
            frenchTitle = mtgCard.frenchTitle,
            castingCost = mtgCard.castingCost,
            `type` = mtgCard.`type`,
            description = mtgCard.description,
            power = mtgCard.power,
            toughness = mtgCard.toughness,
            formats = mtgCard.formats,
            artists = mtgCard.artists,
            collectorNumber = publication.collectorNumber,
            edition = publication.edition,
            editionCode = publication.editionCode,
            editionReleaseDate = publication.editionReleaseDate,
            stdEditionCode = publication.stdEditionCode,
            rarity = publication.rarity,
            rarityCode = publication.rarityCode,
            image = publication.image,
            editionImage = publication.editionImage,
            price = publication.price
          )
        }.sortBy(sort)
      }
  }

  private def sort(mtgSetCard: MTGSetCard): Int = {
    def toInt(chars: Seq[Char]) = chars.mkString.toInt * 100

    mtgSetCard.collectorNumber.toSeq match {
      case number :+ letter if letter.isLetter => toInt(number) + letter.toInt
      case number => toInt(number)
    }
  }
}
