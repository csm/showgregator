package org.showgregator.service.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import scala.concurrent.Future

case class TransientUser(email: String,
                         id: UUID)

object TransientUser {
  def insertUser(user: TransientUser): Future[(ResultSet, ResultSet)] = {
    TransientUserRecord.insertUser(user).zip(ReverseTransientUserRecord.insertUser(user))
  }
}

sealed class TransientUserRecord extends CassandraTable[TransientUserRecord, TransientUser] {
  object email extends StringColumn(this) with PartitionKey[String]
  object id extends UUIDColumn(this)

  override def fromRow(r: Row): TransientUser = TransientUser(email(r), id(r))
}

sealed class ReverseTransientUserRecord extends CassandraTable[ReverseTransientUserRecord, TransientUser] {
  object email extends StringColumn(this)
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  override def fromRow(r: Row): TransientUser = TransientUser(email(r), id(r))
}

object TransientUserRecord extends TransientUserRecord with Connector {
  override val tableName = "transient_users"

  def forEmail(email: String): Future[Option[TransientUser]] = {
    select.where(_.email eqs email).one()
  }

  private[TransientUser] def insertUser(user: TransientUser): Future[ResultSet] = {
    insert.value(_.email, user.email)
      .value(_.id, user.id)
      .future()
  }
}

object ReverseTransientUserRecord extends ReverseTransientUserRecord with Connector {
  override val tableName = "reverse_transient_users"

  def forUuid(id: UUID): Future[Option[TransientUser]] = {
    select.where(_.id eqs id).one()
  }

  private[TransientUser] def insertUser(user: TransientUser): Future[ResultSet] = {
    insert.value(_.email, user.email)
      .value(_.id, user.id)
      .future()
  }
}
