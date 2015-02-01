package org.showgregator.service.view

import java.text.DateFormatSymbols

import org.joda.time.DateTime
import com.twitter.finatra.View
import org.showgregator.service.model._

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
class MonthView(val date:DateTime, val month: String, val year: Int, weekList: List[List[DateTime]], user: BaseUser, calendar: Calendar, events: Seq[EventInCalendar]) extends BaseUserView(user) {
  def template: String = "templates/month.mustache"

  private val lastYear = date.minusYears(1)
  private val lastMonth = date.minusMonths(1)
  private val nextMonth = date.plusMonths(1)
  private val nextYear = date.plusYears(1)

  val last_year = lastYear.year().get()
  val last_month = DateFormatSymbols.getInstance().getMonths()(lastMonth.monthOfYear().get() - 1)
  val next_month = DateFormatSymbols.getInstance().getMonths()(nextMonth.monthOfYear().get() - 1)
  val next_year = nextYear.year().get()
  val calendar_self = ""
  val last_year_link = s"${lastYear.year().get()}/${lastYear.monthOfYear().get()}"
  val last_month_link = s"${lastMonth.year().get()}/${lastMonth.monthOfYear().get()}"
  val next_year_link = s"${nextYear.year().get()}/${nextYear.monthOfYear().get()}"
  val next_month_link = s"${nextMonth.year().get()}/${nextMonth.monthOfYear().get()}"
  override val navbar_links = user match {
    case u:User => """<a href="/home">Home</a> | <a href="/calendars">Shared Calendars</a> | <a href="/inbox">Messages</a>"""
    case u:TransientUser => """<a href="/calendars">Shared Calendars</a>"""
  }

  val weeks = weekList.map(week => week.map(day => {
    Map("day" -> day.dayOfMonth().get(),
      "day_classes" -> (if (day.monthOfYear().get() == date.monthOfYear().get())
        "" else " padding-month"),
      "day_link" -> s"${day.year().get}/${day.monthOfYear().get()}/${day.dayOfMonth().get()}")
  }))
}
