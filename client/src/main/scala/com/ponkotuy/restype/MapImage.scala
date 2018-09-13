package com.ponkotuy.restype

import com.ponkotuy.data.master

import com.netaporter.uri.Uri
import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query

import scala.util.Try
import scala.util.matching.Regex

import org.json4s.native.Serialization.write
import org.json4s.native.JsonMethods.parse

/**
 * @author kPherox
 * DAte: 18/09/12.
 */
trait MapData {
  def regexp: Regex

  def parseUrl(uri: Uri): Option[(Int, Int, Int)] = {
    Try {
      uri.path match {
        case this.regexp(id, no, suffix) => (id.toInt, no.toInt, suffix.toInt)
        case this.regexp(id, no) => (id.toInt, no.toInt, 0)
      }
    }.toOption
  }

  def extractNumber(str: String): Option[Int] = Try {
    str.filter { c => '0' <= c && c <= '9' }.toInt
  }.toOption
}

/**
 * Map image
 */
object MapImage extends ResType with Resources with Media with MapData {
  def regexp: Regex = """\A/kcs2/resources/map/(\d+)/(\d+)_image(\d+)?.png\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").flatMap(extractNumber).getOrElse(DefaultVer)
    parseUrl(q.uri).filterNot { case (areaId, infoNo, suffix) => MFGHttp.existsMap(areaId, infoNo, suffix, ver) }.map { case (areaId, infoNo, suffix) =>
      val png = allRead(q.responseContent)
      FilePostable(s"/image/map/${areaId}/${infoNo}/${suffix}/${ver}", "map", 2, png, "png")
    }.toList
  }
}

/**
 * Map image sprite data
 */
object MapImageSprite extends ResType with Resources with MapData {
  def regexp: Regex = """\A/kcs2/resources/map/(\d+)/(\d+)_image(\d+)?.json\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").flatMap(extractNumber).getOrElse(DefaultVer)
    parseUrl(q.uri).map { case (areaId, infoNo, suffix) =>
      val json = parse(q.resCont)
      val result = master.MapFrame.fromJson(json \ "frames", areaId, infoNo, suffix, ver)
      NormalPostable(s"/map_data", write(result), 2) :: Nil
    }.getOrElse(Nil)
  }
}

/**
 * Map image cell position
 */
object MapImageInfo extends ResType with Resources with MapData {
  def regexp: Regex = """\A/kcs2/resources/map/(\d+)/(\d+)_info(\d+)?.json\z""".r

  def postables(q: Query): Seq[Result] = {
    val ver = q.uri.query.param("version").flatMap(extractNumber).getOrElse(DefaultVer)
    parseUrl(q.uri).map { case (areaId, infoNo, suffix) =>
      val json = parse(q.resCont)
      val result = master.CellPosition.fromJson(json \ "spots", areaId, infoNo, suffix, ver)
      NormalPostable(s"/cell_position", write(result), 2) :: Nil
    }.getOrElse(Nil)
  }
}
