package org.showgregator.service.model

import com.datastax.driver.core.{ResultSet, Row}
import com.twitter.util.Future
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.{PrimaryKey, PartitionKey}

case class City(name: String, county: Option[String], state: String, country: String, latitude: Option[Double], longitude: Option[Double])

object City {
  def findCounty(name: String, state: String)(implicit session: Session): Future[Option[String]] = {
    CityRecord.select(_.county)
      .where(_.state eqs state)
      .and(_.name eqs name)
      .get()
      .map {
        case Some(county) => county
        case None => None
      }
  }
}

sealed class CityRecord extends CassandraTable[CityRecord, City] {
  object state extends StringColumn(this) with PartitionKey[String]
  object county extends OptionalStringColumn(this)
  object name extends StringColumn(this) with PrimaryKey[String]
  object country extends StringColumn(this)
  object latitude extends OptionalDoubleColumn(this)
  object longitude extends OptionalDoubleColumn(this)

  override def fromRow(r: Row): City = City(name(r), county(r), state(r), country(r), latitude(r), longitude(r))
}

object CityRecord extends CityRecord with Connector {
  override val tableName = "cities"

  def insertCity(city: City)(implicit session: Session): Future[ResultSet] = {
    insert
      .ifNotExists()
      .value(_.name, city.name)
      .value(_.county, city.county)
      .value(_.state, city.state)
      .value(_.country, city.country)
      .value(_.latitude, city.latitude)
      .value(_.longitude, city.longitude)
      .execute()
  }
}