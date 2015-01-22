package org.showgregator.service.model

import java.util.UUID
import org.joda.time.DateTime
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, UUIDColumn}
import com.websudos.phantom.keys.{Descending, ClusteringOrder, PartitionKey}
import com.websudos.phantom.column.DateTimeColumn
import com.datastax.driver.core.Row
import scala.concurrent.Future

case class EventInCalendar(calendar: UUID, event: UUID, when: DateTime, title: String) {
  def getCalendar: Future[Option[Calendar]] = {
    CalendarRecord.getById(calendar)
  }

  def getEvent: Future[Option[Event]] = {
    EventRecord.getById(event)
  }
}

sealed class EventInCalendarRecord extends CassandraTable[EventInCalendarRecord, EventInCalendar] {
  object calendar extends UUIDColumn(this) with PartitionKey[UUID]
  object event extends UUIDColumn(this) with PartitionKey[UUID]
  object when extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending
  object title extends StringColumn(this)

  def fromRow(r: Row): EventInCalendar = EventInCalendar(calendar(r), event(r), when(r), title(r))
}