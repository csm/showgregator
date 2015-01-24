package org.showgregator.service.model

import java.util.UUID
import com.datastax.driver.core.Row
import org.joda.time.{Duration, Period, DateTime}

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, UUIDColumn}
import com.websudos.phantom.keys.{Descending, ClusteringOrder, PartitionKey}
import com.websudos.phantom.column.{MapColumn, DateTimeColumn}
import scala.concurrent.Future
import com.websudos.phantom.Implicits._

case class Event(id: UUID,
                 when: DateTime,
                 title: String,
                 venueId: UUID,
                 link: Option[String],
                 info: String,
                 acl: Map[String, Int])

sealed class EventRecord extends CassandraTable[EventRecord, Event] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object when extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending
  object title extends StringColumn(this)
  object venueId extends UUIDColumn(this)
  object link extends OptionalStringColumn(this)
  object info extends StringColumn(this)
  object acl extends MapColumn[EventRecord, Event, String, Int](this)

  def fromRow(r: Row): Event = Event(id(r), when(r), title(r), venueId(r), link(r), info(r), acl(r))
}

object EventRecord extends EventRecord with Connector {
  override val tableName = "events"

  def getById(id: UUID)(implicit session:Session): Future[Option[Event]] = {
    select.where(_.id eqs id).one()
  }

  /**
   * Insert an event into the database. The TTL is how long the event will stick
   * around AFTER the date of the event.
   *
   * If this event exists in the past, and the TTL is not long enough such that
   * the event date plus the TTL is before the current date, then the event is not
   * inserted.
   *
   * @param event
   * @param ttl
   * @return
   */
  def insertEvent(event: Event, ttl: Period = Period.days(60))(implicit session:Session): Future[Option[ResultSet]] = {
    val ttlSeconds = new Duration(DateTime.now(), event.when.plus(ttl)).toStandardSeconds.getSeconds
    if (ttlSeconds > 0) {
      insert.value(_.id, event.id)
        .value(_.when, event.when)
        .value(_.title, event.title)
        .value(_.venueId, event.venueId)
        .value(_.link, event.link)
        .value(_.info, event.info)
        .value(_.acl, event.acl)
        .ttl(ttlSeconds)
        .future()
        .map(rs => Some(rs))
    } else {
      Future.successful(None)
    }
  }
}