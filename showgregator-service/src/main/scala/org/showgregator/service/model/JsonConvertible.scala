package org.showgregator.service.model

import scala.util.parsing.json.JSONObject

/**
 * Created by cmarshall on 1/18/15.
 */
trait JsonConvertible {
  def toJson:JSONObject
}
