package org.showgregator.service.model

import java.util.UUID

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import com.twitter.util.Future
import org.showgregator.core.PasswordHashing

case class RegisterToken(token: UUID, email: Option[String])

sealed class RegisterTokenRecord extends CassandraTable[RegisterTokenRecord, RegisterToken] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object email extends OptionalStringColumn(this)

  def fromRow(r: Row): RegisterToken = RegisterToken(id(r), email(r))
}

object RegisterTokenRecord extends RegisterTokenRecord with Connector {
  override val tableName = "register_tokens"

  def findToken(token: UUID)(implicit session: Session): Future[Option[RegisterToken]] = {
    select.where(_.id eqs token).get()
  }

  def takeToken(token: RegisterToken,
                email: String,
                handle: Option[String],
                password: Array[Char])(implicit session: Session): Future[Option[PendingUser]] = {
    for {
      user <- Future(PendingUser(UUID.randomUUID(), UUID.randomUUID(), email, handle, PasswordHashing(password)))
      batchResult <- {
        val batch = BatchStatement()
        batch.add(PendingUserRecord.prepareInsert(user))
        batch.add(ReversePendingUserRecord.prepareInsert(user))
        batch.add(delete.where(_.id eqs token.token))
        batch.execute().map(_.wasApplied())
      }
    } yield if (batchResult)
      Some(user)
    else
      None
  }
}