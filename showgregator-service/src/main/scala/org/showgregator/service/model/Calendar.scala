package org.showgregator.service.model

import java.util.UUID

import com.datastax.driver.core.Row

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{StringColumn, UUIDColumn}
import com.websudos.phantom.keys.PartitionKey
import com.websudos.phantom.column.MapColumn

case class Calendar(id: UUID, title: String, acl: Map[String, Int])

sealed class CalendarRecord extends CassandraTable[CalendarRecord, Calendar] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object title extends StringColumn(this)
  object acl extends MapColumn[CalendarRecord, Calendar, String, Int](this)

  def fromRow(r: Row): Calendar = Calendar(id(r), title(r), acl(r))
}