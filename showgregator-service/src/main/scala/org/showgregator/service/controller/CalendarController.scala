package org.showgregator.service.controller

import com.twitter.util.Future
import org.showgregator.service.session.SessionStore
import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import com.twitter.finatra.Request
import org.showgregator.service.model._
import org.showgregator.service.view.{BadRequestView, ForbiddenView, MonthView}
import java.text.DateFormatSymbols
import org.joda.time.DateTime
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper

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
    !!! { (request: Request, user) =>
      user match {
        case u: User =>
          val year = Integer.parseInt(request.routeParams.get("year").get)
          val month = Integer.parseInt(request.routeParams.get("month").get)
          val monthName = DateFormatSymbols.getInstance().getMonths()(month - 1)
          val calMonth = new CalendarMonth(year, month)
          for {
            userCalendar <- UserCalendarRecord.getCalendar(u.id).asFinagle
            calendar <- userCalendar match {
              case Some(uc) => uc.getCalendar.asFinagle
              case None => Future.value(None)
            }
            // TODO query events in the calendar, or fail if calendar not found.
            response <- render.view(new MonthView(calMonth.startOfMonth, monthName, year, calMonth.allDays, user)).toFuture
          } yield response
        case _ =>
          render.view(new BadRequestView("Only registered users have personal calendars")).status(400).toFuture
      }
    }
  }
}
