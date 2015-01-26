package org.showgregator.service.model

import org.showgregator.core.HashedPassword
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import org.showgregator.core.ByteBuffers.AsByteArray
import org.joda.time.{Days, Duration}
import scala.concurrent.Future
import java.nio.ByteBuffer

case class PendingUser(id: UUID, email: String, handle: Option[String], hashedPassword: HashedPassword)

sealed class PendingUserRecord extends CassandraTable[PendingUserRecord, PendingUser] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object email extends StringColumn(this)
  object handle extends OptionalStringColumn(this)
  object alg extends StringColumn(this)
  object salt extends BlobColumn(this)
  object iterations extends IntColumn(this)
  object hash extends BlobColumn(this)

  def fromRow(r: Row): PendingUser = PendingUser(id(r), email(r), handle(r),
    HashedPassword( alg(r), salt(r).asBytes, iterations(r), hash(r).asBytes))
}

object PendingUserRecord extends PendingUserRecord with Connector {
  override val tableName = "pending_users"

  def insertUser(user: PendingUser, ttl: Duration = Duration.standardDays(15))(implicit session:Session):Future[ResultSet] = {
    insert.value(_.id, user.id)
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
   * Verifies a user. Inserts a live User record, and deletes this PendingUser.
   * @param id The pending user token.
   * @param session The session.
   * @return A future yielding true if the validation succeeded.
   */
  def verifyUser(id: UUID)(implicit session: Session): Future[Boolean] = {
    for {
      user <- getPendingUser(id)
      insertion <- user match {
        case Some(u) => UserRecord.insertUser(User(u.email, u.handle, u.hashedPassword)).map(_ => true)
        case None => Future.successful(false)
      }
      deletion <- insertion match {
        case true => PendingUserRecord.delete.where(_.id eqs id).future().map(_ => true)
        case false => Future.successful(false)
      }
    } yield deletion
  }
}