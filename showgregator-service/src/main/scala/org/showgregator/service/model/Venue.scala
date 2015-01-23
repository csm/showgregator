package org.showgregator.service.model

import java.util.UUID

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{OptionalStringColumn, DoubleColumn, StringColumn, UUIDColumn}
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row
import scala.concurrent.Future
import com.websudos.phantom.Implicits._

case class Venue(id: UUID,
                 name: String,
                 longitude: Double,
                 latitude: Double,
                 street: String,
                 street2: Option[String],
                 city: String,
                 state: String,
                 country: String)

sealed class VenueRecord extends CassandraTable[VenueRecord, Venue] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)
  object longitude extends DoubleColumn(this)
  object latitude extends DoubleColumn(this)
  object street extends StringColumn(this)
  object street2 extends OptionalStringColumn(this)
  object city extends StringColumn(this)
  object state extends StringColumn(this)
  object country extends StringColumn(this)

  def fromRow(r: Row): Venue = Venue(id(r), name(r), longitude(r), latitude(r), street(r), street2(r), city(r), state(r), country(r))
}

object VenueRecord extends VenueRecord with Connector {
  override val tableName = "venues"

  def getById(id: UUID)(implicit session:Session): Future[Option[Venue]] = {
    select.where(_.id eqs id).one()
  }

  def insertVenue(venue: Venue)(implicit session:Session): Future[ResultSet] = {
    insert.value(_.id, venue.id)
      .value(_.name, venue.name)
      .value(_.longitude, venue.longitude)
      .value(_.latitude, venue.latitude)
      .value(_.street, venue.street)
      .value(_.street2, venue.street2)
      .value(_.city, venue.city)
      .value(_.state, venue.state)
      .value(_.country, venue.country)
      .future()
  }
}