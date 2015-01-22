package org.showgregator.service.model

import java.util.UUID
import com.datastax.driver.core.Row
import org.joda.time.DateTime

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, UUIDColumn}
import com.websudos.phantom.keys.{Descending, ClusteringOrder, PartitionKey}
import com.websudos.phantom.column.{MapColumn, DateTimeColumn}

case class Event(id: UUID,
                 when: DateTime,
                 title: String,
                 venueId: UUID,
                 link: String,
                 info: String,
                 acl: Map[String, Int])

sealed class EventRecord extends CassandraTable[EventRecord, Event] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object when extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending
  object title extends StringColumn(this)
  object venueId extends UUIDColumn(this)
  object link extends StringColumn(this)
  object info extends StringColumn(this)
  object acl extends MapColumn[EventRecord, Event, String, Int](this)

  def fromRow(r: Row): Event = Event(id(r), when(r), title(r), venueId(r), link(r), info(r), acl(r))
}