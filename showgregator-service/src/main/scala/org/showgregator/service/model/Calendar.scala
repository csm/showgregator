package org.showgregator.service.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.keys.PartitionKey
import com.websudos.phantom.column.MapColumn
import com.websudos.phantom.Implicits._
import scala.concurrent.Future

object CalendarRecord extends CalendarRecord with Connector {
  def getById(id: UUID)(implicit session:Session): Future[Option[Calendar]] = {
    select.where(_.id eqs id).one()
  }

  def insertCalendar(calendar:Calendar)(implicit session:Session):Future[ResultSet] = {
    insert.value(_.id, calendar.id)
      .value(_.title, calendar.title)
      .value(_.acl, calendar.acl)
      .future()
  }

  def deleteCalendar(id: UUID)(implicit session:Session): Future[ResultSet] = {
    delete.where(_.id eqs id).future()
  }
}

case class Calendar(id: UUID, title: String, acl: Map[UUID, Int])

sealed class CalendarRecord extends CassandraTable[CalendarRecord, Calendar] {
  override val tableName = "calendars"

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object title extends StringColumn(this)
  object acl extends MapColumn[CalendarRecord, Calendar, UUID, Int](this)

  def fromRow(r: Row): Calendar = Calendar(id(r), title(r), acl(r))
}