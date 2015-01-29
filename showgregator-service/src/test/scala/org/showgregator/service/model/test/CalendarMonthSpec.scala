package org.showgregator.service.model.test

import org.scalatest.{Matchers, FlatSpec}
import org.showgregator.service.model.CalendarMonth

class CalendarMonthSpec extends FlatSpec with Matchers {
  "January 2015" should "span five weeks" in {
    val january = new CalendarMonth(2015, 1)
    january.allDays.size should be (5)
    january.allDays.map(_.size) should be (List(7, 7, 7, 7, 7))
  }

  "January 2015" should "have the first on a Thursday" in {
    val january = new CalendarMonth(2015, 1)
    january.allDays.head(3).year().get() should be (2015)
    january.allDays.head(3).monthOfYear().get() should be (1)
    january.allDays.head(3).dayOfMonth().get() should be (1)
  }

  "January 2015" should "be padded with December at the start" in {
    val january = new CalendarMonth(2015, 1)
    for (i <- Range(0, 3)) {
      january.allDays.head(i).year().get() should be (2014)
      january.allDays.head(i).monthOfYear().get() should be (12)
    }
  }
}
