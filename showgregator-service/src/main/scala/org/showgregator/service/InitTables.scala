package org.showgregator.service

import com.datastax.driver.core.Cluster
import org.showgregator.service.model._
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
object InitTables extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val cluster = Cluster.builder()
    .addContactPoint("127.0.0.1")
    .build()
  implicit val session = try {
    cluster.connect("showgregator")
  } catch {
    case _ => {
      cluster.connect().execute("CREATE KEYSPACE showgregator WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}")
      cluster.connect("showgregator")
    }
  }

  val f = for {
    calendars <- CalendarRecord.create.future().map(_.wasApplied())
    events <- EventRecord.create.future().map(_.wasApplied())
    eventComments <- EventCommentRecord.create.future().map(_.wasApplied())
    eventsInCalendar <- EventInCalendarRecord.create.future().map(_.wasApplied())
    transientUsers <- TransientUserRecord.create.future().map(_.wasApplied())
    reverseTransientUsers <- ReverseTransientUserRecord.create.future().map(_.wasApplied())
    users <- UserRecord.create.future().map(_.wasApplied())
    venues <- VenueRecord.create.future().map(_.wasApplied())
    registerTokens <- RegisterTokenRecord.create.future().map(_.wasApplied())
    pendingUsers <- PendingUserRecord.create.future().map(_.wasApplied())
    reversePendingUsers <- ReversePendingUserRecord.create.future().map(_.wasApplied())
    userEmails <- UserEmailRecord.create.future().map(_.wasApplied())
    userCalendarRecord <- UserCalendarRecord.create.future().map(_.wasApplied())
    emailChangeRecord <- PendingEmailChangeRecord.create.future().map(_.wasApplied())
    passwordChange <- PasswordChangeRecord.create.future().map(_.wasApplied())
  } yield (calendars,
      events,
      eventComments,
      eventsInCalendar,
      transientUsers,
      reverseTransientUsers,
      users,
      venues,
      registerTokens,
      pendingUsers,
      reversePendingUsers,
      userEmails,
      userCalendarRecord,
      emailChangeRecord,
      passwordChange)
  println(Await.result(f, 1.minute))
  System.exit(0)
}
