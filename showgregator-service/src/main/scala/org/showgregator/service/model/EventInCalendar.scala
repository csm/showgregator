package org.showgregator.service.model

import org.joda.time._
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.keys.{Descending => KeysDescending, ClusteringOrder, PartitionKey}
import com.websudos.phantom.column.DateTimeColumn
import scala.concurrent.Future
import com.websudos.phantom.Implicits._

case class EventInCalendar(calendar: UUID, event: UUID, when: DateTime, title: String) {
  def getCalendar(implicit session:Session): Future[Option[Calendar]] = {
    CalendarRecord.getById(calendar)
  }

  def getEvent(implicit session:Session): Future[Option[Event]] = {
    EventRecord.getById(event)
  }
}

sealed class EventInCalendarRecord extends CassandraTable[EventInCalendarRecord, EventInCalendar] {
  object calendar extends UUIDColumn(this) with PartitionKey[UUID]
  object event extends UUIDColumn(this)
  object when extends DateTimeColumn(this) with PrimaryKey[DateTime] with ClusteringOrder[DateTime] with KeysDescending
  object title extends StringColumn(this)

  def fromRow(r: Row): EventInCalendar = EventInCalendar(calendar(r), event(r), when(r), title(r))
}

object EventInCalendarRecord extends EventInCalendarRecord with Connector {
  override val tableName = "events_in_calendar"

  def fetchForCalendar(calendarId: UUID, fromDate: DateTime, toDate: DateTime = DateTime.now(DateTimeZone.UTC))(implicit session:Session): Future[Seq[EventInCalendar]] = {
    select.where(_.calendar eqs calendarId)
      .and(_.when gte fromDate)
      .and(_.when lte toDate)
      .fetch()
  }

  def insertEvent(event: EventInCalendar, ttl: Duration = Days.days(60).toStandardDuration): Future[Option[ResultSet]] = {
    val eventTtl = new Duration(DateTime.now(), event.when.plus(ttl)).toStandardSeconds.getSeconds
    if (eventTtl > 0) {
      insert.value(_.calendar, event.calendar)
        .value(_.event, event.event)
        .value(_.when, event.when)
        .value(_.title, event.title)
        .ttl(eventTtl)
        .future()
        .map(Some(_))
    } else Future.successful(None)
  }
}