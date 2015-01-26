package org.showgregator.service.model

import org.showgregator.core.HashedPassword
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{IntColumn, BlobColumn, StringColumn}
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import java.nio.ByteBuffer

import org.showgregator.core.ByteBuffers.AsByteArray
import scala.concurrent.Future
import com.websudos.phantom.Implicits._

abstract class BaseUser(val email:String)

case class User(_email: String, handle: Option[String], hashedPassword: HashedPassword) extends BaseUser(_email)

sealed class UserRecord extends CassandraTable[UserRecord, User] {
  // the id field will be the email address, downcased.
  object id extends StringColumn(this) with PartitionKey[String]
  object email extends StringColumn(this)
  object handle extends OptionalStringColumn(this)
  object alg extends StringColumn(this)
  object salt extends BlobColumn(this)
  object iterations extends IntColumn(this)
  object hash extends BlobColumn(this)

  def fromRow(r: Row): User = User(email(r), handle(r),
    HashedPassword(alg(r), salt(r).asBytes, iterations(r), hash(r).asBytes))
}

object UserRecord extends UserRecord with Connector {
  override val tableName = "users"

  def getByEmail(email: String)(implicit session:Session): Future[Option[User]] = {
    select.where(_.id eqs email.toLowerCase).one()
  }

  def insertUser(user: User)(implicit session:Session): Future[ResultSet] = {
    insert.value(_.id, user.email.toLowerCase)
      .value(_.email, user.email)
      .value(_.handle, user.handle)
      .value(_.alg, user.hashedPassword.alg)
      .value(_.salt, ByteBuffer.wrap(user.hashedPassword.salt))
      .value(_.iterations, user.hashedPassword.iterations)
      .value(_.hash, ByteBuffer.wrap(user.hashedPassword.hash))
      .future()
  }
}
