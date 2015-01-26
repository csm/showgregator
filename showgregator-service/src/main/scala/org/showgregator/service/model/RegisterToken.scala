package org.showgregator.service.model

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import scala.concurrent.Future

case class RegisterToken(token: UUID, email: Option[String])

sealed class RegisterTokenRecord extends CassandraTable[RegisterTokenRecord, RegisterToken] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object email extends OptionalStringColumn(this)

  def fromRow(r: Row): RegisterToken = RegisterToken(id(r), email(r))
}

object RegisterTokenRecord extends RegisterTokenRecord with Connector {
  override val tableName = "register_tokens"

  def findToken(token: UUID)(implicit session: Session): Future[Option[RegisterToken]] = {
    select.where(_.id eqs token).one()
  }
}