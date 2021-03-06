package org.showgregator.service.model

import java.util.UUID

import org.showgregator.core.crypto.{PasswordHashing, HashedPassword}
import org.showgregator.core.geo.USLocales.{States, County, City}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import org.showgregator.core.util.ByteBuffers.AsByteArray
import org.joda.time.Duration
import com.twitter.util.Future
import java.nio.ByteBuffer

case class PendingUser(id: UUID,
                       user: UUID,
                       email: String,
                       handle: Option[String],
                       hashedPassword: HashedPassword,
                       city: Option[City],
                       timeZoneId: Option[String],
                       transientEmail: Option[String] = None,
                       transientId: Option[UUID] = None)

object PendingUser {
  def createUser(email: String, handle: Option[String], password: Array[Char],
                 userId: Option[UUID], transientEmail: Option[String] = None, transientId: Option[UUID] = None,
                 city: Option[City] = None, timeZoneId: Option[String] = None)(implicit session: Session): Future[Option[PendingUser]] = {
    for {
      user <- Future(PendingUser(UUID.randomUUID(), userId.getOrElse(UUID.randomUUID()), email, handle, PasswordHashing(password),
        city, timeZoneId, transientEmail, transientId))
      insert <- {
        val batch = BatchStatement()
        batch.add(PendingUserRecord.prepareInsert(user))
        batch.add(ReversePendingUserRecord.prepareInsert(user))
        batch.execute()
      }
    } yield if (insert.wasApplied())
      Some(user)
    else
      None
  }
}

sealed class PendingUserRecord extends CassandraTable[PendingUserRecord, PendingUser] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object user extends UUIDColumn(this)
  object email extends StringColumn(this)
  object handle extends OptionalStringColumn(this)
  object alg extends StringColumn(this)
  object salt extends BlobColumn(this)
  object iterations extends IntColumn(this)
  object hash extends BlobColumn(this)
  object transientEmail extends OptionalStringColumn(this)
  object transientId extends OptionalUUIDColumn(this)
  // City, todo, make this a UDT
  object city extends OptionalStringColumn(this)
  object county extends OptionalStringColumn(this)
  object state extends OptionalStringColumn(this)
  object timezone extends OptionalStringColumn(this)

  def fromRow(r: Row): PendingUser = PendingUser(id(r), user(r), email(r), handle(r),
    HashedPassword(alg(r), salt(r).asBytes, iterations(r), hash(r).asBytes),
    (city(r), county(r), state(r)) match {
      case (Some(name), Some(county), Some(state)) => States.forAbbrev(state).flatMap(st => Some(City(name, County(county, st))))
      case _ => None
    }, timezone(r), transientEmail(r), transientId(r))
}

object PendingUserRecord extends PendingUserRecord with Connector {
  override val tableName = "pending_users"

  def insertUser(user: PendingUser, ttl: Duration = Duration.standardDays(15))(implicit session:Session):Future[ResultSet] = {
    prepareInsert(user, ttl)
      .execute()
  }

  def getPendingUser(id: UUID)(implicit session: Session): Future[Option[PendingUser]] = {
    select.where(_.id eqs id).get()
  }

  def prepareInsert(user: PendingUser, ttl: Duration = Duration.standardDays(15)) = {
    insert.value(_.id, user.id)
      .value(_.user, user.user)
      .value(_.email, user.email)
      .value(_.handle, user.handle)
      .value(_.alg, user.hashedPassword.alg)
      .value(_.salt, ByteBuffer.wrap(user.hashedPassword.salt))
      .value(_.iterations, user.hashedPassword.iterations)
      .value(_.hash, ByteBuffer.wrap(user.hashedPassword.hash))
      .value(_.transientEmail, user.transientEmail)
      .value(_.transientId, user.transientId)
      .value(_.city, user.city.map(_.name))
      .value(_.county, user.city.map(_.county.name))
      .value(_.state, user.city.map(_.county.state.abbrev))
      .value(_.timezone, user.timeZoneId)
      .ttl(ttl.toStandardSeconds.getSeconds)
  }

  def prepareDelete(id: UUID) = {
    PendingUserRecord.delete.where(_.id eqs id)
  }

  /**
   * Verifies a user. Inserts a live User record, adds a calendar for the user, and deletes this PendingUser.
   *
   * @param id The pending user token.
   * @param session The session.
   * @return A future yielding true if promoting a pending user to a real user succeeded.
   */
  def verifyUser(id: UUID)(implicit session: Session): Future[Boolean] = {
    for {
      pendingUser <- getPendingUser(id)
      transientUser <- pendingUser match {
        case Some(pu) if pu.transientEmail.isDefined => TransientUserRecord.forEmail(pu.transientEmail.get)
        case _ => Future.value(None)
      }
      createUser <- pendingUser match {
        case Some(pu) => {
          val batch = BatchStatement()
          val calendar = Calendar(UUID.randomUUID(), "", Map(pu.user -> (CalendarPermissions.AllPermission & ~CalendarPermissions.Share)))
          val user = User(pu.user, pu.email, pu.handle, pu.hashedPassword, pu.city, pu.timeZoneId)
          User.prepareCreate(batch, user, calendar)
          batch.add(prepareDelete(pu.id))
          batch.add(ReversePendingUserRecord.prepareDelete(pu.email))
          if (transientUser.isDefined) {
            batch.add(TransientUserRecord.prepareDelete(transientUser.get.email))
            batch.add(ReverseTransientUserRecord.prepareDelete(transientUser.get.id))
          }
          batch.execute().map(_.wasApplied())
        }
        case None => Future.value(false)
      }
    } yield createUser
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
    prepareInsert(user)
      .execute()
  }

  def prepareInsert(user: PendingUser) = {
    insert.value(_.id, user.email.toLowerCase)
      .value(_.tokenId, user.id)
      .value(_.email, user.email)
  }

  def getByEmail(email: String)(implicit session: Session): Future[Option[ReversePendingUser]] = {
    select.where(_.id eqs email.toLowerCase).get()
  }

  def prepareDelete(email: String) = ReversePendingUserRecord.delete.where(_.id eqs email.toLowerCase)
}