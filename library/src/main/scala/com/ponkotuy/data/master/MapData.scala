package com.ponkotuy.data.master

import org.json4s._

/**
 *
 * @author kPherox
 * Date: 18/09/13
 */
case class MapFrame(
  areaId: Int, infoNo: Int, suffix: Int, name: String, posX: Int, posY: Int, width: Int, height: Int, version: Int
)


object MapFrame {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, suffix: Int, version: Int): List[MapFrame] = {
    val JObject(obj) = json
    obj.map { case JField(name, data) =>
      val frameName = name.stripPrefix(s"map${"%03d".format(areaId)}${"%02d".format(infoNo)}_")
      data.extractOpt[MapFrameData].map(_.frame.build(areaId, infoNo, suffix, frameName, version))
    }.flatten
  }

  private case class MapFrameData(frame: RawMapFrame)

  private case class RawMapFrame(
    x: Int, y: Int, w: Int, h: Int
  ) {
    def build(areaId: Int, infoNo: Int, suffix: Int, name: String, version: Int): MapFrame =
      MapFrame(areaId, infoNo, suffix, name, x, y, w, h, version)
  }
}

case class CellPosition(
  areaId: Int, infoNo: Int, suffix: Int, cell: Int, posX: Int, posY: Int, version: Int
)

object CellPosition {
  implicit val formats = DefaultFormats

  def fromJson(json: JValue, areaId: Int, infoNo: Int, suffix: Int, version: Int): List[CellPosition] = {
    json.extractOrElse[List[RawCellPosition]](Nil).map(_.build(areaId, infoNo, suffix, version))
  }

  private case class RawCellPosition(
    no: Int, x: Int, y: Int
  ) {
    def build(areaId: Int, infoNo: Int, suffix: Int, version: Int): CellPosition =
      CellPosition(areaId, infoNo, suffix, no, x, y, version)
  }
}
