package org.showgregator.service.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import com.twitter.util.Future

case class TransientUser(_email: String,
                         _userId: UUID,
                         id: UUID = null)
  extends BaseUser(_userId, _email)

case class ReverseTransientUser(email: String, id: UUID)

object TransientUser {
  def insertUser(user: TransientUser)(implicit session:Session): Future[ResultSet] = {
    val batch = BatchStatement()
    batch.add(TransientUserRecord.prepareInsert(user))
    batch.add(ReverseTransientUserRecord.prepareInsert(user))
    batch.execute()
  }
}

sealed class TransientUserRecord extends CassandraTable[TransientUserRecord, TransientUser] {
  object eid extends StringColumn(this) with PartitionKey[String]
  object email extends StringColumn(this)
  object userId extends UUIDColumn(this)
  object id extends UUIDColumn(this)

  override def fromRow(r: Row): TransientUser = TransientUser(email(r), userId(r), id(r))
}

sealed class ReverseTransientUserRecord extends CassandraTable[ReverseTransientUserRecord, ReverseTransientUser] {
  object email extends StringColumn(this)
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  override def fromRow(r: Row): ReverseTransientUser = ReverseTransientUser(email(r), id(r))
}

object TransientUserRecord extends TransientUserRecord with Connector {
  override val tableName = "transient_users"

  def forEmail(email: String)(implicit session:Session): Future[Option[TransientUser]] = {
    select.where(_.eid eqs email.toLowerCase).get()
  }

  def insertUser(user: TransientUser)(implicit session:Session): Future[ResultSet] = {
    prepareInsert(user)
      .execute()
  }

  def prepareInsert(user: TransientUser) = {
    insert.value(_.eid, user.email.toLowerCase)
      .value(_.email, user.email)
      .value(_.id, user.id)
      .value(_.userId, user.userId)
  }

  def prepareDelete(email: String) = delete.where(_.eid eqs email.toLowerCase)
}

object ReverseTransientUserRecord extends ReverseTransientUserRecord with Connector {
  override val tableName = "reverse_transient_users"

  def forUuid(id: UUID)(implicit session:Session): Future[Option[TransientUser]] = {
    select.where(_.id eqs id).get().flatMap({
      case Some(rtu) => TransientUserRecord.forEmail(rtu.email)
      case None => Future.value(None)
    })
  }

  def insertUser(user: TransientUser)(implicit session:Session): Future[ResultSet] = {
    prepareInsert(user)
      .execute()
  }

  def prepareInsert(user: TransientUser) = {
    insert.value(_.email, user.email)
      .value(_.id, user.id)
  }

  def prepareDelete(id: UUID) = delete.where(_.id eqs id)
}
