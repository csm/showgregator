package org.showgregator.service.view

import org.joda.time.DateTime
import com.twitter.finatra.View

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
class MonthView(val month: String, val year: Int, val weeks: List[List[DateTime]]) extends View {
  def template: String = "templates/month.mustache"
}
