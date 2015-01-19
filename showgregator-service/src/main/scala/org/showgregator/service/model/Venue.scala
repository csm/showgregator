package org.showgregator.service.model

import java.util.UUID

import scala.util.parsing.json.JSONObject

case class Venue(id: UUID, name: String) extends JsonConvertible {
  override def toJson: JSONObject = JSONObject(Map(
    "id" -> id.toString,
    "name" -> name
  ))
}
