package org.showgregator.service.view

import org.joda.time.DateTime
import org.showgregator.service.model.BaseUser

/**
 * Created by cmarshall on 2/3/15.
 */
class DayView(date: DateTime, user: BaseUser) extends BaseUserView(user) {
  override def template: String = "templates/day.mustache"

  val local_date = date.toString("M d, Y")
}
