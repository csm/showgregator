package org.showgregator.service.model

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{UUIDColumn, UUID}
import com.websudos.phantom.keys.PartitionKey

case class PasswordChange(tokenId: UUID, user: UUID)

sealed class PasswordChangeRecord extends CassandraTable[PasswordChangeRecord, PasswordChange] {
  object tokenId extends UUIDColumn(this) with PartitionKey[UUID]
  object user extends UUIDColumn(this)

  override def fromRow(r: Row): PasswordChange = PasswordChange(tokenId(r), user(r))
}

object PasswordChangeRecord extends PasswordChangeRecord with Connector {
  override val tableName: String = "password_change_tokens"
}