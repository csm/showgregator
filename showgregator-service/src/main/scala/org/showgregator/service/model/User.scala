package org.showgregator.service.model

import java.util.UUID

import org.joda.time.DateTimeZone
import org.showgregator.core.geo.USLocales
import org.showgregator.core.geo.USLocales.{City, States, County}
import org.showgregator.core.crypto.{HashedPassword, PasswordHashing}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{IntColumn, BlobColumn, StringColumn}
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import java.nio.ByteBuffer

import org.showgregator.core.util.ByteBuffers.AsByteArray
import com.twitter.util.Future
import com.websudos.phantom.Implicits._

/**
 * BaseUser encompasses registered Users and unregistered TransientUsers.
 * @param userId
 * @param email
 */
abstract class BaseUser(val userId: UUID, val email:String) {
  def timeZone(implicit session: Session): DateTimeZone
}

case class User(id: UUID, _email: String, handle: Option[String], hashedPassword: HashedPassword = null,
                city: Option[City], timeZoneId: Option[String]) extends BaseUser(id, _email) {
  def withEmail(newEmail: String): User = {
    User(id, newEmail, handle, hashedPassword, city, timeZoneId)
  }

  override def timeZone(implicit session: Session): DateTimeZone = {
    timeZoneId.map(DateTimeZone.forID).orElse(
      city.flatMap(c => USLocales.TimeZones.findZoneForCounty(c.county)).map(DateTimeZone.forID))
    .getOrElse(DateTimeZone.UTC)
  }

  def withCity(newCity: Option[City]): User = {
    User(id, email, handle, hashedPassword, newCity, timeZoneId)
  }

  def withTimeZone(newTimeZone: Option[String]): User = {
    User(id, email, handle, hashedPassword, city, newTimeZone)
  }
}

sealed class UserRecord extends CassandraTable[UserRecord, User] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object email extends StringColumn(this) with PrimaryKey[String] with ClusteringOrder[String] with Ascending
  object handle extends OptionalStringColumn(this)
  object alg extends StringColumn(this)
  object salt extends BlobColumn(this)
  object iterations extends IntColumn(this)
  object hash extends BlobColumn(this)
  object city extends OptionalStringColumn(this)
  object county extends OptionalStringColumn(this)
  object state extends OptionalStringColumn(this)
  object timezone extends OptionalStringColumn(this)

  def fromRow(r: Row): User = User(id(r), email(r), handle(r),
    HashedPassword(alg(r), salt(r).asBytes, iterations(r), hash(r).asBytes),
      (city(r), county(r), state(r)) match {
        case (Some(city), Some(county), Some(state)) => States.forAbbrev(state).map(st => City(city, County(county, st)))
        case (None, Some(county), Some(state)) => States.forAbbrev(state).map(st => City("", County(county, st)))
        case _ => None
      }, timezone(r))
}

object User {
  val NullId = new UUID(0, 0)

  def prepareCreate(batch: BatchStatement, user: User, calendar: Calendar) = {
    batch
      .add(prepareInsert(user))
      .add(UserEmailRecord.prepareInsert(UserEmail(user.email, user.userId)))
      .add(CalendarRecord.prepareInsert(calendar))
      .add(UserCalendarRecord.prepareInsert(UserCalendar(user.userId, calendar.id)))
  }

  def createUser(id: UUID, email: String, handle: Option[String], password: Array[Char])(implicit session: Session): Future[Option[User]] = {
    val calendar = Calendar(UUID.randomUUID(), "", Map(id -> (CalendarPermissions.AllPermission & ~CalendarPermissions.Share)))
    for {
      user <- Future(User(id, email, handle, PasswordHashing(password), None, None))
      insertBatch <- {
        val batch = BatchStatement()
        prepareCreate(batch, user, calendar)
        batch.execute()
      }
    } yield if (insertBatch.wasApplied())
      Some(user)
    else
      None
  }

  def prepareInsert(user: User) = {
    UserRecord
      .insert
      //.ifNotExists() can't use this, we can't batch our inserts
      .value(_.id, user.id)
      .value(_.email, user.email)
      .value(_.handle, user.handle)
      .value(_.alg, user.hashedPassword.alg)
      .value(_.iterations, user.hashedPassword.iterations)
      .value(_.salt, ByteBuffer.wrap(user.hashedPassword.salt))
      .value(_.hash, ByteBuffer.wrap(user.hashedPassword.hash))
      .value(_.city, user.city.map(_.name))
      .value(_.county, user.city.map(_.county.name))
      .value(_.state, user.city.map(_.county.state.abbrev))
      .value(_.timezone, user.timeZoneId)
  }

  def prepareDelete(id: UUID, email: String) = {
    UserRecord
      .delete
      .where(_.id eqs id)
      .and(_.email eqs email)
  }

  def insertUser(user: User)(implicit session: Session): Future[Boolean] = {
    prepareInsert(user).execute().map(_.wasApplied())
  }

  /**
   * Update a user's email address.
   *
   * Performs the following steps:
   * 1. deletes the existing user object.
   * 2. inserts a new user with a different email.
   * 3. deletes the old email -> id mapping.
   * 4. inserts the new email -> id mapping.
   *
   * @param user The existing user.
   * @param email The new email.
   * @param session The session.
   * @return A future that will contain the updated user if the statements were applied.
   */
  def updateUserEmail(user: User, email: String)(implicit session: Session): Future[Option[User]] = {
    val updated = User(user.userId, email, user.handle, user.hashedPassword, user.county, user.timeZoneId)
    BatchStatement()
      .add(prepareDelete(user.userId, user.email))
      .add(prepareInsert(updated))
      .add(UserEmailRecord.prepareDelete(user.email))
      .add(UserEmailRecord.prepareInsert(UserEmail(email, user.userId)))
      .execute()
      .map(rs => {
        if (rs.wasApplied())
          Some(updated)
        else
          None
      })
  }

  def updateUserHandle(user: User, handle:Option[String])(implicit session: Session): Future[Option[User]] = {
    UserRecord.update
      .where(_.id eqs user.id)
      .and(_.email eqs user.email)
      .modify(_.handle setTo handle)
      .execute()
      .map(rs => if (rs.wasApplied()) {
        Some(User(user.id, user.email, handle, user.hashedPassword, user.city, user.timeZoneId))
      } else {
        None
      })
  }

  def updateUserPassword(user: User, password:Array[Char])(implicit session: Session): Future[Option[User]] = {
    for {
      hash <- Future(PasswordHashing(password))
      update <- UserRecord.update
        .where(_.id eqs user.id)
        .and(_.email eqs user.email)
        .modify(_.alg setTo hash.alg)
        .and(_.iterations setTo hash.iterations)
        .and(_.salt setTo ByteBuffer.wrap(hash.salt))
        .and(_.hash setTo ByteBuffer.wrap(hash.hash))
        .execute()
    } yield if (update.wasApplied()) {
      Some(User(user.id, user.email, user.handle, hash, user.city, user.timeZoneId))
    } else {
      None
    }
  }

  def updateUserCounty(user: User, city: Option[City])(implicit session: Session): Future[Option[User]] = {
    UserRecord.update
      .where(_.id eqs user.id)
      .and(_.email eqs user.email)
      .modify(_.city setTo city.map(_.name))
      .and(_.county setTo city.map(_.county.name))
      .and(_.state setTo city.map(_.county.state.abbrev))
      .execute()
      .map(rs => if (rs.wasApplied()) Some(user.withCity(city)) else None)
  }

  def updateUserTimeZone(user: User, timeZone: Option[String])(implicit session: Session): Future[Option[User]] = {
    UserRecord.update
      .where(_.id eqs user.id)
      .and(_.email eqs user.email)
      .modify(_.timezone setTo timeZone)
      .execute()
      .map(rs => if (rs.wasApplied()) Some(user.withTimeZone(timeZone)) else None)
  }
}

case class UserEmail(email: String, id: UUID)

sealed class UserEmailRecord extends CassandraTable[UserEmailRecord, UserEmail] {
  object eid extends StringColumn(this) with PartitionKey[String]
  object email extends StringColumn(this)
  object id extends UUIDColumn(this)

  def fromRow(r: Row): UserEmail = UserEmail(email(r), id(r))
}

object UserRecord extends UserRecord with Connector {
  override val tableName = "users"

  def getByEmail(email: String)(implicit session:Session): Future[Option[User]] = {
    for {
      userEmail <- UserEmailRecord.getByEmail(email)
      user <- userEmail match {
        case Some(ue) => getByID(ue.id)
        case None => Future.value(None)
      }
    } yield user
  }

  def getByID(id: UUID)(implicit session:Session): Future[Option[User]] = {
    select.where(_.id eqs id).get()
  }

  def insertUser(user: User)(implicit session:Session): Future[ResultSet] = {
    User.prepareInsert(user).execute()
  }
}

object UserEmailRecord extends UserEmailRecord with Connector {
  override def tableName: String = "user_emails"

  def getByEmail(email: String)(implicit session:Session): Future[Option[UserEmail]] = {
    select.where(_.eid eqs email.toLowerCase).get()
  }

  def insertUserEmail(userEmail: UserEmail)(implicit session: Session): Future[ResultSet] = {
    prepareInsert(userEmail).execute()
  }

  def prepareInsert(userEmail: UserEmail) = {
    UserEmailRecord
      .insert
      //.ifNotExists()
      .value(_.eid, userEmail.email.toLowerCase)
      .value(_.email, userEmail.email)
      .value(_.id, userEmail.id)
  }

  def prepareDelete(email: String) = {
    UserEmailRecord
      .delete
      .where(_.eid eqs email.toLowerCase)
  }
}
