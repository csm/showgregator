package org.showgregator.service.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.keys.PartitionKey
import com.websudos.phantom.column.MapColumn
import com.websudos.phantom.Implicits._
import com.twitter.util.Future

object CalendarRecord extends CalendarRecord with Connector {
  def getById(id: UUID)(implicit session:Session): Future[Option[Calendar]] = {
    select.where(_.id eqs id).get()
  }

  def insertCalendar(calendar:Calendar)(implicit session:Session):Future[ResultSet] = {
    insert.value(_.id, calendar.id)
      .value(_.title, calendar.title)
      .value(_.acl, calendar.acl)
      .execute()
  }

  def deleteCalendar(id: UUID)(implicit session:Session): Future[ResultSet] = {
    delete.where(_.id eqs id).execute()
  }

  def prepareInsert(calendar: Calendar) = {
    CalendarRecord.insert
      .value(_.id, calendar.id)
      .value(_.title, calendar.title)
      .value(_.acl, calendar.acl)
  }
}

case class Calendar(id: UUID, title: String, acl: Map[UUID, Int]) {
  def hasPermission(userId: UUID, perms: Int):Boolean = {
    acl.get(userId).exists(p => (p & perms) == perms) || anonymousPermission(perms)
  }

  def anonymousPermission(perms: Int): Boolean = {
    acl.get(User.NullId).exists(p => (p & perms) == perms)
  }
}

sealed class CalendarRecord extends CassandraTable[CalendarRecord, Calendar] {
  override val tableName = "calendars"

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object title extends StringColumn(this)
  object acl extends MapColumn[CalendarRecord, Calendar, UUID, Int](this)

  def fromRow(r: Row): Calendar = Calendar(id(r), title(r), acl(r))
}