package org.showgregator.service.controller

import java.util.UUID

import com.twitter.util.Future
import org.showgregator.service.session.SessionStore
import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import com.twitter.finatra.Request
import org.showgregator.service.model._
import org.showgregator.service.view.{ServerErrorView, BadRequestView, ForbiddenView, MonthView}
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
  get("/calendar/:id/today") {
    !!! {
      (request, user) => {
        val now = DateTime.now()
        redirect(s"/calendar/${request.routeParams.get("id").get}/${now.year().get()}/${now.monthOfYear().get()}").toFuture
      }
    }
  }

  get("/calendar/:id/:year/:month") {
    !!! {
      (request, user) => {
        val year = request.routeParams.get("year").map(Integer.parseInt)
        val month = request.routeParams.get("month").map(Integer.parseInt)
        require(year.isDefined)
        require(month.isDefined && month.get >= 1 && month.get <= 12)
        val calMonth = new CalendarMonth(year.get, month.get)
        val monthName = DateFormatSymbols.getInstance().getMonths()(month.get - 1)

        if (request.routeParams.get("id").get.equals("mine")) {
          user match {
            case u: User => UserCalendarRecord.getCalendar(user.userId)
              .flatMap({
                case Some(uc) => for {
                  calendar <- CalendarRecord.getById(uc.calendar)
                  events <- calendar match {
                    case Some(cal) => EventInCalendarRecord.fetchForCalendar(cal.id, calMonth.allDays.head.head, calMonth.allDays.last.last)
                    case None => Future.value(Seq())
                  }
                  response <- calendar match {
                    case Some(cal) => if (cal.hasPermission(user.userId, CalendarPermissions.Read)) {
                      render.view(new MonthView(calMonth.startOfMonth, monthName, year.get, calMonth.allDays, user, cal, events, Some("mine"))).toFuture
                    } else {
                      render.static("/html/401.html").status(401).toFuture
                    }
                    case None => render.view(new ServerErrorView("Damn, we were unable to find your calendar.")).status(500).toFuture
                  }
                } yield response

                case None => render.view(new ServerErrorView("Damn, we were unable to find your calendar.")).status(500).toFuture
            })
            case _ =>
              render.view(new BadRequestView("Only registered users have personal calendars")).status(400).toFuture
          }
        } else {
          val id = request.routeParams.get("id").map(UUID.fromString)
          for {
            calendar <- CalendarRecord.getById(id.get)
            events <- calendar match {
              case Some(cal) => EventInCalendarRecord.fetchForCalendar(cal.id, calMonth.allDays.head.head, calMonth.allDays.last.last)
              case None => Future.value(Seq())
            }
            response <- calendar match {
              case Some(cal) => if (cal.hasPermission(user.userId, CalendarPermissions.Read)) {
                render.view(new MonthView(calMonth.startOfMonth, monthName, year.get, calMonth.allDays, user, cal, events)).toFuture
              } else {
                render.static("/html/401.html").status(401).toFuture
              }
              case None => render.view(new ServerErrorView("Damn, we were unable to find that calendar.")).status(500).toFuture
            }
          } yield response
        }
      }
    }
  }

  get("/calendar/:id/:year/:month/:day") {
    !!! { (request, user) =>
      val year = request.routeParams.get("year").map(Integer.parseInt)
      val month = request.routeParams.get("month").map(Integer.parseInt)
      val day = request.routeParams.get("day").map(Integer.parseInt)
      require(year.isDefined)
      require(month.isDefined && month.get >= 1 && month.get <= 12)
      val date = new DateTime(year.get, month.get, day.get, 0, 0)

      throw new UnsupportedOperationException("not done yet");
    }
  }
}
