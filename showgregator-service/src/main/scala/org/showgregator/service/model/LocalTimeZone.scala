package org.showgregator.service.model

import com.datastax.driver.core.Row
import com.twitter.util.Future
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.{PrimaryKey, PartitionKey}
import org.joda.time.DateTimeZone

case class LocalTimeZone(country: String, state: Option[String], city: Option[String], zoneId: String) {
  def timeZone:DateTimeZone = DateTimeZone.forID(zoneId)
}

object LocalTimeZone {
  def find(city: City)(implicit session: Session): Future[Option[LocalTimeZone]] = {
    for {
      tz1 <- LocalTimeZoneRecord.select
        .where(_.country eqs city.country)
        .and(_.state eqs city.state)
        .and(_.city eqs city.name)
        .get()
      tz2 <- LocalTimeZoneRecord.select
        .where(_.country eqs city.country)
        .and(_.state eqs city.state)
        .and(_.city eqs "")
        .get()
      tz3 <- LocalTimeZoneRecord.select
        .where(_.country eqs city.country)
        .and(_.state eqs "")
        .and(_.city eqs "")
        .get()
    } yield tz1 orElse tz2 orElse tz3
  }
}

sealed class LocalTimeZoneRecord extends CassandraTable[LocalTimeZoneRecord, LocalTimeZone] {
  object country extends StringColumn(this) with PartitionKey[String]
  object state extends StringColumn(this) with PartitionKey[String]
  object city extends StringColumn(this) with PartitionKey[String]
  object zoneId extends StringColumn(this)

  override def fromRow(r: Row): LocalTimeZone = LocalTimeZone(country(r),
    state(r) match {
      case "" => None
      case s => Some(s)
    }, city(r) match {
      case "" => None
      case s => Some(s)
    }, zoneId(r))
}

object LocalTimeZoneRecord extends LocalTimeZoneRecord with Connector {
  override val tableName = "local_time_zones"
}