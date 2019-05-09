package fr.gstraymond.stats

import java.util.Date
import java.util.concurrent.TimeUnit

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, _}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

import scala.concurrent.duration.Duration

object Timing {

  def apply[A](name: String)(computation: => A): Timing[A] = new Timing().apply(name, computation)
}

case class ProcessStats(name: String, duration: String, result: String)

class Timing[A] {

  private var stats: Seq[ProcessStats] = Seq.empty

  private var result: Option[A] = None

  def apply(name: String, computation: => A): Timing[A] = {
    val start = new Date().getTime
    result = Some(computation)
    val end = new Date().getTime
    val resultAsString = get match {
      case seq: Seq[_] => s"${seq.size} elements"
      case _ => s"${get.getClass} result"
    }
    val processStats2 = ProcessStats(
      name,
      Duration(end - start, TimeUnit.MILLISECONDS).formatted("%s"),
      resultAsString
    )
    stats = stats :+ processStats2

    this
  }

  def get: A = result.get

  // FIXME: howto pass name ?
  def map[B](f: A => B): Timing[B] = {
    flatten(Timing[B](stats.last.name)(f(get)))
  }

  def flatMap[B](f: A => Timing[B]): Timing[B] = {
    flatten(f(get))
  }

  def flatten[B](implicit other: Timing[B]): Timing[B] = {
    other.stats = stats ++ other.stats
    other
  }

  implicit val StatsCode: JsonValueCodec[Stats] = JsonCodecMaker.make[Stats](CodecMakerConfig(transientEmpty = false))
  case class Stats(stats: Seq[ProcessStats])
  def json: String = {
    writeToString(Stats(stats), WriterConfig(indentionStep = 2))
  }
}