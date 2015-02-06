package org.showgregator.service.model

import com.datastax.driver.core.{ResultSet, Row}
import com.twitter.util.Future
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits.{OptionalDoubleColumn, DoubleColumn, Session, StringColumn}
import com.websudos.phantom.keys.{PrimaryKey, PartitionKey}

case class County(name: String, state: String, country: String)
case class City(name: String, county: String, state: String, country: String, latitude: Option[Double], longitude: Option[Double])

sealed class CountyRecord extends CassandraTable[CountyRecord, County] {
  object name extends StringColumn(this) with PrimaryKey[String]
  object state extends StringColumn(this) with PartitionKey[String]
  object country extends StringColumn(this)

  override def fromRow(r: Row): County = County(name(r), state(r), country(r))
}

object CountyRecord extends CountyRecord with Connector {
  override val tableName = "counties"

  def insertCounty(county: County)(implicit session: Session): Future[ResultSet] = {
    insert
      .ifNotExists()
      .value(_.name, county.name)
      .value(_.state, county.state)
      .value(_.country, county.country)
      .execute()
  }
}

sealed class CityRecord extends CassandraTable[CityRecord, City] {
  object state extends StringColumn(this) with PartitionKey[String]
  object county extends StringColumn(this)
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