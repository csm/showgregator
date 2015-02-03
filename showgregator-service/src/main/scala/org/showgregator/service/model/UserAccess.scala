package org.showgregator.service.model

import java.nio.ByteBuffer
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.datastax.driver.core.{ResultSet, Row}
import com.twitter.util.Future
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{Session, StringColumn, UUIDColumn, BlobColumn}
import com.websudos.phantom.column.DateTimeColumn
import com.websudos.phantom.keys.{Ascending, ClusteringOrder, PrimaryKey, PartitionKey}
import org.joda.time.{Duration, DateTime}

/**
 * Class for tracking a user's access path through the site, but anonymously.
 *
 * @param id
 * @param when
 * @param uri
 */
case class UserAccess(id: UUID, when: DateTime, uri: String)

object UserAccess {
  private val HashKey = "Han Solo Shot First!".getBytes

  def apply(user: UUID, session: UUID, uri: String, when: DateTime = DateTime.now()): UserAccess = {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(new SecretKeySpec(HashKey, "HmacSHA1"))
    update(mac, user.getMostSignificantBits)
    update(mac, user.getLeastSignificantBits)
    update(mac, session.getMostSignificantBits)
    update(mac, session.getLeastSignificantBits)
    val hash = mac.doFinal()
    hash(6) = ((hash(6) & 0x0f) | 0x50).toByte
    hash(8) = ((hash(8) & 0x3f) | 0x80).toByte
    val buffer = ByteBuffer.wrap(hash)
    val id = new UUID(buffer.getLong, buffer.getLong)
    UserAccess(id, when, uri)
  }

  private def update(mac: Mac, value: Long) = {
    mac.update((value >> 56).toByte)
    mac.update((value >> 48).toByte)
    mac.update((value >> 40).toByte)
    mac.update((value >> 32).toByte)
    mac.update((value >> 24).toByte)
    mac.update((value >> 16).toByte)
    mac.update((value >> 8).toByte)
    mac.update(value.toByte)
  }
}

sealed class UserAccessRecord extends CassandraTable[UserAccessRecord, UserAccess] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object when extends DateTimeColumn(this) with PrimaryKey[DateTime] with ClusteringOrder[DateTime] with Ascending
  object uri extends StringColumn(this)

  override def fromRow(r: Row): UserAccess = UserAccess(id(r), when(r), uri(r))
}

object UserAccessRecord extends UserAccessRecord with Connector {
  override def tableName: String = "user_access_records"

  def insertAccess(access: UserAccess, ttl: Duration = Duration.standardDays(7))(implicit session: Session): Future[ResultSet] = {
    insert
      .value(_.id, access.id)
      .value(_.when, access.when)
      .value(_.uri, access.uri)
      .ttl(ttl.getStandardSeconds.toInt)
      .execute()
  }
}