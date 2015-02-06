package org.showgregator.service.model

import com.twitter.util.Future
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.{PrimaryKey, PartitionKey}

case class IPV4GeoMap(base:Int,
                      mask:Int,
                      nameId:Option[Long],
                      countryId:Option[Long],
                      countryRep:Option[Long],
                      isAnonymous:Boolean,
                      isSatellite:Boolean,
                      postalCode: Option[String],
                      latitude:Option[Double],
                      longitude:Option[Double])

sealed class IPV4GeoMapRecord extends CassandraTable[IPV4GeoMapRecord, IPV4GeoMap] {
  object base extends IntColumn(this) with PartitionKey[Int]
  object mask extends IntColumn(this) with PrimaryKey[Int]
  object nameId extends OptionalLongColumn(this)
  object countryId extends OptionalLongColumn(this)
  object countryRep extends OptionalLongColumn(this)
  object isAnonymous extends BooleanColumn(this)
  object isSatellite extends BooleanColumn(this)
  object postalCode extends OptionalStringColumn(this)
  object latitude extends OptionalDoubleColumn(this)
  object longitude extends OptionalDoubleColumn(this)

  override def fromRow(r: Row): IPV4GeoMap = IPV4GeoMap(base(r), mask(r), nameId(r), countryId(r), countryRep(r),
    isAnonymous(r), isSatellite(r), postalCode(r), latitude(r), longitude(r))
}

object IPV4GeoMapRecord extends IPV4GeoMapRecord with Connector {
  override val tableName = "maxmind_ipv4_geo"

  def insertMap(map: IPV4GeoMap)(implicit session:Session): Future[ResultSet] = {
    insert
      .value(_.base, map.base)
      .value(_.mask, map.mask)
      .value(_.nameId, map.nameId)
      .value(_.countryId, map.countryId)
      .value(_.countryRep, map.countryRep)
      .value(_.isAnonymous, map.isAnonymous)
      .value(_.isSatellite, map.isSatellite)
      .value(_.postalCode, map.postalCode)
      .value(_.latitude, map.latitude)
      .value(_.longitude, map.longitude)
      .execute()
  }
}