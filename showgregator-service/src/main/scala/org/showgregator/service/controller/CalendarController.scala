package org.showgregator.service.controller

import org.showgregator.service.session.SessionStore
import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import com.twitter.finatra.Request
import org.showgregator.service.model.{CalendarRecord, CalendarMonth}
import org.showgregator.service.view.MonthView
import java.text.DateFormatSymbols
import org.joda.time.DateTime

object CalendarController {
  def monthName(month:Int) = {
    val months = DateFormatSymbols.getInstance().getMonths
    months(month)
  }
}

class CalendarController(override implicit val sessionStore:SessionStore,
                         override implicit val session: Session,
                         override implicit val context: ExecutionContext)
  extends AuthenticatedController {
  get("/calendar/today") { _ =>
    val now = DateTime.now()
    redirect(s"/calendar/${now.year().get()}/${now.monthOfYear().get()}").toFuture
  }

  get("/calendar/:year/:month") {
    !!! { (request:Request, user) =>
      val year = Integer.parseInt(request.routeParams.get("year").get)
      val month = Integer.parseInt(request.routeParams.get("month").get)
      val monthName = DateFormatSymbols.getInstance().getMonths()(month - 1)
      val calMonth = new CalendarMonth(year, month)
      render.view(new MonthView(calMonth.startOfMonth, monthName, year, calMonth.allDays, user)).toFuture
    }
  }
}
