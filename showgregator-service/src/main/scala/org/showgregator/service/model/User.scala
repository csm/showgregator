package org.showgregator.service.model

import com.websudos.phantom.column.ModifiableColumn
import org.showgregator.core.{PasswordHashing, HashedPassword}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{IntColumn, BlobColumn, StringColumn}
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import java.nio.ByteBuffer

import org.showgregator.core.ByteBuffers.AsByteArray
import scala.concurrent.Future
import com.websudos.phantom.Implicits._

/**
 * BaseUser encompasses registered Users and unregistered TransientUsers.
 * @param userId
 * @param email
 */
abstract class BaseUser(val userId: UUID, val email:String)

case class User(id: UUID, _email: String, handle: Option[String], hashedPassword: HashedPassword = null) extends BaseUser(id, _email) {
  def withEmail(newEmail: String): User = {
    User(id, newEmail, handle, hashedPassword)
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

  def fromRow(r: Row): User = User(id(r), email(r), handle(r),
    HashedPassword(alg(r), salt(r).asBytes, iterations(r), hash(r).asBytes))
}

object User {
  def insertUser(user: User)(implicit session: Session): Future[Boolean] = {
    for {
      insertUser <- UserRecord.insert.ifNotExists()
        .value(_.id, user.id)
        .value(_.email, user.email)
        .value(_.handle, user.handle)
        .value(_.alg, user.hashedPassword.alg)
        .value(_.salt, ByteBuffer.wrap(user.hashedPassword.salt))
        .value(_.iterations, user.hashedPassword.iterations)
        .value(_.hash, ByteBuffer.wrap(user.hashedPassword.hash))
        .future()
      insertEmail <- if(insertUser.wasApplied()) {
        UserEmailRecord.insert
          .value(_.email, user.email)
          .value(_.id, user.id)
          .future()
          .map(rs => rs.wasApplied())
      } else Future.successful(false)
    } yield insertEmail
  }

  def updateUserEmail(user: User, email: String)(implicit session: Session): Future[Option[User]] = {
    for {
      deleteOldUser <- UserRecord.delete
        .where(_.id eqs user.id)
        .and(_.email eqs user.email)
        .future()
        .map(_.wasApplied())
      updateUser <- if (deleteOldUser) {
        UserRecord.insert
          .value(_.id, user.id)
          .value(_.email, email)
          .value(_.handle, user.handle)
          .value(_.alg, user.hashedPassword.alg)
          .value(_.salt, ByteBuffer.wrap(user.hashedPassword.salt))
          .value(_.iterations, user.hashedPassword.iterations)
          .value(_.hash, ByteBuffer.wrap(user.hashedPassword.hash))
          .future()
          .map(_.wasApplied())
      } else {
        Future.successful(false)
      }
      deleteOld <- if (updateUser) {
        UserEmailRecord.delete.where(_.eid eqs user.email.toLowerCase).future().map(_.wasApplied())
      } else Future.successful(false)
      insertNew <- if (updateUser) {
        UserEmailRecord.insert.value(_.eid, email.toLowerCase)
          .value(_.email, email)
          .value(_.id, user.id)
          .future()
          .map(_.wasApplied())
      } else Future.successful(false)
    } yield if (insertNew)
      Some(user.withEmail(email))
    else
      None
  }

  def updateUserHandle(user: User, handle:Option[String]):Future[Option[User]] = {
    UserRecord.update
      .where(_.id eqs user.id)
      .modify(_.handle setTo handle)
      .future()
      .map(rs => if (rs.wasApplied()) {
        Some(User(user.id, user.email, handle, user.hashedPassword))
      } else {
        None
      })
  }

  def updateUserPassword(user: User, password:Array[Char]):Future[Option[User]] = {
    for {
      hash <- Future(PasswordHashing(password))
      update <- UserRecord.update
        .where(_.id eqs user.id)
        .modify(_.alg setTo hash.alg)
        .and(_.iterations setTo hash.iterations)
        .and(_.salt setTo ByteBuffer.wrap(hash.salt))
        .and(_.hash setTo ByteBuffer.wrap(hash.hash))
        .future()
    } yield if (update.wasApplied()) {
      Some(User(user.id, user.email, user.handle, hash))
    } else {
      None
    }
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
        case None => Future.successful(None)
      }
    } yield user
  }

  def getByID(id: UUID)(implicit session:Session): Future[Option[User]] = {
    select.where(_.id eqs id).one()
  }

  def insertUser(user: User)(implicit session:Session): Future[ResultSet] = {
    insert.value(_.id, user.id)
      .value(_.email, user.email)
      .value(_.handle, user.handle)
      .value(_.alg, user.hashedPassword.alg)
      .value(_.salt, ByteBuffer.wrap(user.hashedPassword.salt))
      .value(_.iterations, user.hashedPassword.iterations)
      .value(_.hash, ByteBuffer.wrap(user.hashedPassword.hash))
      .future()
  }
}

object UserEmailRecord extends UserEmailRecord with Connector {
  override def tableName: String = "user_emails"

  def getByEmail(email: String)(implicit session:Session): Future[Option[UserEmail]] = {
    select.where(_.eid eqs email.toLowerCase).one()
  }

  def insertUserEmail(userEmail: UserEmail)(implicit session: Session): Future[ResultSet] = {
    insert.value(_.eid, userEmail.email.toLowerCase)
      .value(_.email, userEmail.email)
      .value(_.id, userEmail.id)
      .future()
  }
}
