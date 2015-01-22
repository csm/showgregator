package org.showgregator.service.model

import java.util.UUID

import scala.util.parsing.json.JSONObject
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{OptionalStringColumn, DoubleColumn, StringColumn, UUIDColumn}
import com.websudos.phantom.keys.PartitionKey
import com.datastax.driver.core.Row

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