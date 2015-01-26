package org.showgregator.service.model

import org.joda.time.{DateTimeConstants, DateTime}

class CalendarDay(date: DateTime, month: Int) {
  def isInMonth: Boolean = date.getMonthOfYear == month
}

class CalendarMonth(year: Int, month: Int) {
  require(year > 0)
  require(month > 0 && month <= 12)
  val startOfMonth = new DateTime(year, month, 1, 0, 0)
  val endOfMonth = startOfMonth.dayOfMonth().withMaximumValue()
  val firstDayOfWeek = startOfMonth.dayOfWeek().get()
  val endDayOfWeek = endOfMonth.dayOfWeek().get()

  val allDays = {
    val padBefore = Range(DateTimeConstants.MONDAY, firstDayOfWeek).map {
      day => startOfMonth.minusDays(firstDayOfWeek - day)
    }.toList
    val padAfter = Range(endDayOfWeek, DateTimeConstants.SUNDAY).reverse.map {
      day => endOfMonth.plusDays(DateTimeConstants.SUNDAY - day)
    }
    val days = Stream.from(0).map(i => startOfMonth.plusDays(i)).takeWhile(d => !d.isAfter(endOfMonth))
    (padBefore ++ days ++ padAfter).grouped(7).toList
  }
}
