package org.showgregator.service.model

import org.showgregator.core.HashedPassword
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{IntColumn, BlobColumn, StringColumn}
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import java.nio.ByteBuffer

import org.showgregator.core.ByteBuffers.AsByteArray

case class User(email: String, handle: String, hashedPassword: HashedPassword)

sealed class UserRecord extends CassandraTable[UserRecord, User] {
  object email extends StringColumn(this) with PartitionKey[String]
  object handle extends StringColumn(this)
  object alg extends StringColumn(this)
  object salt extends BlobColumn(this)
  object iterations extends IntColumn(this)
  object hash extends BlobColumn(this)

  def fromRow(r: Row): User = User(email(r), handle(r), HashedPassword(alg(r), salt(r).asBytes, iterations(r), hash(r).asBytes))
}