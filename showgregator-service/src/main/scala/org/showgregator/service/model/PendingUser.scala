package org.showgregator.service.model

import java.util.UUID

import org.showgregator.core.HashedPassword
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import org.showgregator.core.ByteBuffers.AsByteArray
import org.joda.time.Duration
import scala.concurrent.Future
import java.nio.ByteBuffer

case class PendingUser(id: UUID, user: UUID, email: String, handle: Option[String], hashedPassword: HashedPassword)

sealed class PendingUserRecord extends CassandraTable[PendingUserRecord, PendingUser] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object user extends UUIDColumn(this)
  object email extends StringColumn(this)
  object handle extends OptionalStringColumn(this)
  object alg extends StringColumn(this)
  object salt extends BlobColumn(this)
  object iterations extends IntColumn(this)
  object hash extends BlobColumn(this)

  def fromRow(r: Row): PendingUser = PendingUser(id(r), user(r), email(r), handle(r),
    HashedPassword(alg(r), salt(r).asBytes, iterations(r), hash(r).asBytes))
}

object PendingUserRecord extends PendingUserRecord with Connector {
  override val tableName = "pending_users"

  def insertUser(user: PendingUser, ttl: Duration = Duration.standardDays(15))(implicit session:Session):Future[ResultSet] = {
    insert.value(_.id, user.id)
      .value(_.user, user.user)
      .value(_.email, user.email)
      .value(_.handle, user.handle)
      .value(_.alg, user.hashedPassword.alg)
      .value(_.salt, ByteBuffer.wrap(user.hashedPassword.salt))
      .value(_.iterations, user.hashedPassword.iterations)
      .value(_.hash, ByteBuffer.wrap(user.hashedPassword.hash))
      .ttl(ttl.toStandardSeconds.getSeconds)
      .future()
  }

  def getPendingUser(id: UUID)(implicit session: Session): Future[Option[PendingUser]] = {
    select.where(_.id eqs id).one()
  }

  /**
   * Verifies a user. Inserts a live User record, adds a calendar for the user, and deletes this PendingUser.
   * @param id The pending user token.
   * @param session The session.
   * @return A future yielding true if the validation succeeded.
   */
  def verifyUser(id: UUID)(implicit session: Session): Future[Boolean] = {
    val calendarId = UUID.randomUUID()
    for {
      user <- getPendingUser(id)
      insertion <- user match {
        case Some(u) => UserRecord.insertUser(User(u.user, u.email, u.handle, u.hashedPassword)).map(_.wasApplied())
        case None => Future.successful(false)
      }
      addCalendar <- insertion match {
        case true => CalendarRecord.insertCalendar(Calendar(calendarId, "", Map(user.get.user -> CalendarPermissions.Admin))).map(_.wasApplied())
        case false => Future.successful(false)
      }
      addUserCalendar <- addCalendar match {
        case true => UserCalendarRecord.insertCalendar(UserCalendar(user.get.user, calendarId)).map(_.wasApplied())
        case false => Future.successful(false)
      }
      deletion <- insertion match {
        case true => PendingUserRecord.delete.where(_.id eqs id).future().map(_.wasApplied())
        case false => Future.successful(false)
      }
      reverseDeletion <- deletion match {
        case true => ReversePendingUserRecord.delete.where(_.id eqs user.get.email.toLowerCase).future().map(_.wasApplied())
        case false => Future.successful(false)
      }
    } yield addUserCalendar && reverseDeletion
  }
}

case class ReversePendingUser(email: String, token: UUID) {
  def getUser(implicit session: Session):Future[Option[PendingUser]] = {
    PendingUserRecord.getPendingUser(token)
  }
}

sealed class ReversePendingUserRecord extends CassandraTable[ReversePendingUserRecord, ReversePendingUser] {
  object id extends StringColumn(this) with PartitionKey[String]
  object tokenId extends UUIDColumn(this)
  object email extends StringColumn(this)

  def fromRow(r: Row): ReversePendingUser = ReversePendingUser(email(r), tokenId(r))
}

object ReversePendingUserRecord extends ReversePendingUserRecord with Connector {
  override val tableName = "reverse_pending_users"

  def insertUser(user: PendingUser)(implicit session: Session): Future[ResultSet] = {
    insert.value(_.id, user.email.toLowerCase)
      .value(_.tokenId, user.id)
      .value(_.email, user.email)
      .future()
  }

  def getByEmail(email: String)(implicit session: Session): Future[Option[ReversePendingUser]] = {
    select.where(_.id eqs email.toLowerCase).one()
  }
}