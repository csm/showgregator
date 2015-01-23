package org.showgregator.service.model

import java.util.UUID
import org.joda.time.{DateTimeZone, DateTime}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, UUIDColumn}
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
}