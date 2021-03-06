package org.showgregator.service.model

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey

import com.twitter.util.Future

case class UserCalendar(user: UUID, calendar: UUID) {
  def getCalendar(implicit session: Session): Future[Option[Calendar]] = {
    CalendarRecord.getById(calendar)
  }
}

sealed class UserCalendarRecord extends CassandraTable[UserCalendarRecord, UserCalendar] {
  object user extends UUIDColumn(this) with PartitionKey[UUID]
  object calendar extends UUIDColumn(this)

  override def fromRow(r: Row): UserCalendar = UserCalendar(user(r), calendar(r))
}

object UserCalendarRecord extends UserCalendarRecord with Connector {
  override val tableName: String = "user_calendars"

  def insertCalendar(userCalendar: UserCalendar): Future[ResultSet] = {
    insert.value(_.user, userCalendar.user)
      .value(_.calendar, userCalendar.calendar)
      .execute()
  }

  def getCalendar(user: UUID): Future[Option[UserCalendar]] = {
    select.where(_.user eqs user).get()
  }

  def prepareInsert(userCalendar: UserCalendar) = {
    insert.value(_.user, userCalendar.user)
      .value(_.calendar, userCalendar.calendar)
  }
}
