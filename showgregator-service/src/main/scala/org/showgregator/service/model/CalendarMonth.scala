package org.showgregator.service.model

import org.joda.time.{DateTimeConstants, DateTime}

class CalendarDay(date: DateTime, month: Int) {
  def isInMonth: Boolean = date.getMonthOfYear == month
}

class CalendarMonth(year: Int, month: Int) {
  val startOfMonth = new DateTime(year, month, 1, 0, 0)
  val endOfMonth = startOfMonth.dayOfMonth().withMaximumValue()
  val firstDayOfWeek = startOfMonth.dayOfWeek().get()
  val endDayOfWeek = endOfMonth.dayOfWeek().get()

  val padBefore = Range(DateTimeConstants.MONDAY, firstDayOfWeek).map {
    day => startOfMonth.minusDays(firstDayOfWeek - day)
  }.toList
  val padAfter = Range(endDayOfWeek, DateTimeConstants.SUNDAY).reverse.map {
    day => endOfMonth.plusDays(DateTimeConstants.SUNDAY - day)
  }
}
