package fr.gstraymond.logs

import java.io.File
import java.net.URLDecoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import eu.bitwalker.useragentutils.UserAgent
import fr.gstraymond.utils.Log
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.io.Source
import scala.util.Try

object LogParser extends Log {

  val fileLog = "/home/guillaume/logs/elasticsearch.log"

  def parse() = {
    val queries = Source.fromFile(new File(fileLog))
      .getLines()
      .flatMap(parseLine)
      .filter(_.request.startsWith("/magic"))
      .filter(_.request.contains("source="))
      .flatMap(extractContent)
      .filter { rawContent =>
        val values = rawContent.json \\ "from"
        val from = values.headOption.flatMap(_.asOpt[Int])
        from.forall(_ == 0)
      }
      .filter { rawContent =>
        (rawContent.json \ "query" \ "match_all").asOpt[JsValue].isEmpty
      }
      .flatMap(extractQuery)
      .toSeq

    buildReport(queries)
      .foreach(printReports)
  }

  val logEntryPattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"$".r

  def parseLine(line: String): Option[Line] = {
    line match {
      case logEntryPattern(_, _, _, dateTime, request, "200", _, referer, device) =>
        Some(Line(dateTime, request.split(" ")(1), referer, device))
      case _ =>
        //log.error(s"not matched --> $line")
        None
    }
  }

  def extractContent(line: Line): Option[RawContent] = {
    Try {
      val json = URLDecoder.decode(line.request.split("source=")(1), "UTF-8").replace("\\x22", "\"")
      Json.parse(json).as[JsObject]
    }.map { jsObject =>
      val rawContent = RawContent(jsObject, line.date, line.device)
      //log.debug("json: " + (jsObject \ "query" \ "bool" \ "must").asOpt[Seq[JsObject]].map(_.mkString(", ")).getOrElse(jsObject))
      Some(rawContent)
    }.getOrElse {
      log.error(s"url  --> ${line.request}")
      None
    }
  }

  //23/Apr/2015:17:32:19 -0400
  val dtFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)

  def extractQuery(rawContent: RawContent): Option[Query] = {
    val maybeQuery = (rawContent.json \\ "query_string")
      .flatMap(_ \\ "query")
      .flatMap(_.asOpt[String]) match {
      case Seq() => None
      case seq => Some(seq.head)
    }

    val date = ZonedDateTime.parse(rawContent.date, dtFormatter)

    val jsonFacets = (rawContent.json \\ "term").flatMap(_.asOpt[JsObject])

    val query = jsonFacets.foldLeft(Query(maybeQuery, Map.empty, date, rawContent.version)) { (acc, jsObject) =>
      val facets = jsObject.value.foldLeft(acc.facets) { (_acc, tuple) =>
        val (k, v) = tuple
        val term = k.split("\\.").head
        _acc.get(term).map { vs =>
          _acc + (term -> (vs :+ v.as[String]))
        }.getOrElse {
          _acc + (term -> Seq(v.as[String]))
        }
      }
      acc.copy(facets = facets)
    }

    query match {
      case Query(None, map, _, _) if map.isEmpty => None
      case _ => Some(query)
    }
  }

  def buildReport(queries: Seq[Query]): Seq[MonthReport] = {
    queries
      .groupBy { q =>
        val month = q.date.getMonthValue match {
          case m if m < 10 => s"0$m"
          case m => s"$m"
        }
        s"${q.date.getYear}-$month"
      }
      .map { case (yearMonth, groupedQueries) =>
        val queriesWithString = groupedQueries.flatMap(_.maybeQuery)
        val queriesWithFacet = groupedQueries.filter(_.facets.nonEmpty)

        val topQueries = transform(queriesWithString)
        val facets = transform(queriesWithFacet.flatMap(_.facets.keys))
        val versions = transform {
          groupedQueries.map(_.version).map {
            case v if v.startsWith("Android") =>
              v.replace("Android Java/", "") match {
                case vv if vv.length == 5 => vv.dropRight(2)
                case vv => vv
              }
            case v => UserAgent.parseUserAgentString(v).getBrowser.getName
          }
        }

        MonthReport(
          yearMonth = yearMonth,
          totalQueries = groupedQueries.size,
          queriesWithString = queriesWithString.size,
          queriesWithFacet = queriesWithFacet.size,
          topQueries = topQueries,
          facets = facets,
          versions = versions)
      }
      .toSeq
      .sortBy(_.yearMonth)
  }

  private def transform(data: Seq[String]): Seq[(String, Int)] = {
    data
      .groupBy(f => f)
      .mapValues(_.size)
      .toSeq
      .sortBy(-_._2)
      .take(15)
  }

  def printReports(report: MonthReport): Unit = {
    def format(tuples: Seq[(String, Int)]) = {
      val total = tuples.map(_._2).sum
      def percent(size: Int) = size * 100 / total
      tuples.map(top => s"${top._1} [${top._2}-${percent(top._2)}%]").mkString("  ")
    }

    val string =
      s"""
         |--- Report ${report.yearMonth} / totalQueries: ${report.totalQueries} - facets ${report.queriesWithFacet} - query ${report.queriesWithString}
         | - topQueries__ ${format(report.topQueries)}
         | - facets______ ${format(report.facets)}
         | - versions____ ${format(report.versions)}
    """.stripMargin
    println(string)
  }
}

case class Line(
  date: String,
  request: String,
  referer: String,
  device: String
)

case class RawContent(
  json: JsObject,
  date: String,
  version: String
)

case class Query(
  maybeQuery: Option[String],
  facets: Map[String, Seq[String]],
  date: ZonedDateTime,
  version: String
)

case class MonthReport(
  yearMonth: String,
  totalQueries: Int,
  queriesWithString: Int,
  queriesWithFacet: Int,
  topQueries: Seq[(String, Int)],
  facets: Seq[(String, Int)],
  versions: Seq[(String, Int)]
)