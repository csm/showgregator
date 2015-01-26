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
    calendars <- CalendarRecord.create.future()
    events <- EventRecord.create.future()
    eventComments <- EventCommentRecord.create.future()
    eventsInCalendar <- EventInCalendarRecord.create.future()
    transientUsers <- TransientUserRecord.create.future()
    reverseTransientUsers <- ReverseTransientUserRecord.create.future()
    users <- UserRecord.create.future()
    venues <- VenueRecord.create.future()
    registerTokens <- RegisterTokenRecord.create.future()
    pendingUsers <- PendingUserRecord.create.future()
  } yield (calendars, events, eventComments, eventsInCalendar, transientUsers, reverseTransientUsers, users, venues, registerTokens, pendingUsers)
  println(Await.result(f, 1.minute))
  System.exit(0)
}
