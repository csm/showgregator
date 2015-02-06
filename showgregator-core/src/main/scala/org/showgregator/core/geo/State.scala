
package org.showgregator.core.geo

import org.joda.time.{DateTime, DateTimeZone}
import org.showgregator.core.geo.SingleTimeZone

case class City(name: String, county: County)
case class County(name: String, state: State)
case class State(name: String, abbrev: String, country: Country)
case class District(name: String, abbrev: String, country: Country)
case class Territory(name: String, abbrev: String, country: Country)
case class Country(name: String, abbrev: String)

abstract class SummerTime {
  def isSummerTime(date: DateTime): Boolean
}

class USSummerTime(baseZone: DateTimeZone) extends SummerTime {
  override def isSummerTime(date: DateTime): Boolean = {
    val adjustedDate = date.withZone(baseZone)
    val month = adjustedDate.monthOfYear().get()
    val day = adjustedDate.dayOfMonth().get()
    date.year().get() match {
      case 2012 => (month > 3 && month < 11) || (month == 3 && day >= 11) || (month == 11 && day < 4)
      case 2013 => (month > 3 && month < 11) || (month == 3 && day >= 10) || (month == 11 && day < 3)
      case 2014 => (month > 3 && month < 11) || (month == 3 && day >=  9) || (month == 11 && day < 2)
      case 2015 => (month > 3 && month < 11) || (month == 3 && day >=  8);
      case 2016 => (month > 3 && month < 11) || (month == 3 && day >= 13) || (month == 11 && day < 6);
      case 2017 => (month > 3 && month < 11) || (month == 3 && day >= 12) || (month == 11 && day < 5);
      case 2018 => (month > 3 && month < 11) || (month == 3 && day >= 11) || (month == 11 && day < 4);
      case 2019 => (month > 3 && month < 11) || (month == 3 && day >= 10) || (month == 11 && day < 3);
      case 2020 => (month > 3 && month < 11) || (month == 3 && day >=  8);
      case _ => throw new IllegalArgumentException(s"no daylight savings info for year ${date.year().get()}")
    }
  }
}
object ESTSummerTime extends USSummerTime(DateTimeZone.forID("EST"))
object CSTSummerTime extends USSummerTime(DateTimeZone.forID("CST"))
object MSTSummerTime extends USSummerTime(DateTimeZone.forID("MST"))
object PSTSummerTime extends USSummerTime(DateTimeZone.forID("PST"))

abstract class DateVariableTimeZone {
  def timeZoneForDate(date: DateTime):DateTimeZone
}
case class SingleTimeZone(zone: DateTimeZone) extends DateVariableTimeZone {
  override def timeZoneForDate(date: DateTime): DateTimeZone = zone
}
case class DaylightSavingsTimeZone(zone: DateTimeZone, dstZone: DateTimeZone, summerTime: SummerTime) extends DateVariableTimeZone {
  override def timeZoneForDate(date: DateTime): DateTimeZone = {
    if (summerTime.isSummerTime(date)) dstZone
    else zone
  }
}

abstract class GeoTimeZone(val zone: DateTimeZone, val dstZone: Option[DateTimeZone])
case class StateGeoTime(state: State, override val zone: DateTimeZone, override val dstZone: Option[DateTimeZone]) extends GeoTimeZone(zone, dstZone)
case class CountyGeoTime(county: County, override val zone: DateTimeZone, override val dstZone: Option[DateTimeZone]) extends GeoTimeZone(zone, dstZone)

object Countries {
  val USA = Country("The United States of America", "US")
}

object States {
  import Countries._

  val Alabama = State("Alabama", "AL", USA)
  val Alaska = State("Alaska", "AK", USA)
  val Arizona = State("Arizona", "AZ", USA)
  val Arkansas = State("Arkansas", "AR", USA)
  val California = State("California", "CA", USA)
  val Colorado = State("Colorado", "CO", USA)
  val Connecticut = State("Connecticut", "CT", USA)
  val Delaware = State("Delaware", "DE", USA)
  val Florida = State("Florida", "FL", USA)
  val Georgia = State("Georgia", "GA", USA)
  val Hawaii = State("Hawaii", "HI", USA)
  val Idaho = State("Idaho", "ID", USA)
  val Illinois = State("Illinois", "IL", USA)
  val Indiana = State("Indiana", "IN", USA)
  val Iowa = State("Iowa", "IA", USA)
  val Kansas = State("Kansas", "KS", USA)
  val Kentucky = State("Kentucky", "KY", USA)
  val Louisiana = State("Louisiana", "LA", USA)
  val Maine = State("Maine", "ME", USA)
  val Maryland = State("Maryland", "MD", USA)
  val Massachusetts = State("Massachusetts", "MA", USA)
  val Michigan = State("Michigan", "MI", USA)
  val Minnesota = State("Minnesota", "MN", USA)
  val Mississippi = State("Mississippi", "MS", USA)
  val Missouri = State("Missouri", "MO", USA)
  val Montana = State("Montana", "MT", USA)
  val Nebraska = State("Nebraska", "NE", USA)
  val Nevada = State("Nevada", "NV", USA)
  val NewHampshire = State("New Hampshire", "NH", USA)
  val NewJersey = State("New Jersey", "NJ", USA)
  val NewMexico = State("New Mexico", "NM", USA)
  val NewYork = State("New York", "NY", USA)
  val NorthCarolina = State("North Carolina", "NC", USA)
  val NorthDakota = State("North Dakota", "ND", USA)
  val Ohio = State("Ohio", "OH", USA)
  val Oklahoma = State("Oklahoma", "OK", USA)
  val Oregon = State("Oregon", "OR", USA)
  val Pennsylvania = State("Pennsylvania", "PA", USA)
  val RhodeIsland = State("Rhode Island", "RI", USA)
  val SouthCarolina = State("South Carolina", "SC", USA)
  val SouthDakota = State("South Dakota", "SD", USA)
  val Tennessee = State("Tennessee", "TN", USA)
  val Texas = State("Texas", "TX", USA)
  val Utah = State("Utah", "UT", USA)
  val Vermont = State("Vermont", "VT", USA)
  val Virginia = State("Virginia", "VA", USA)
  val Washington = State("Washington", "WA", USA)
  val WestVirginia = State("West Virginia", "WV", USA)
  val Wisconsin = State("Wisconsin", "WI", USA)
  val Wyoming = State("Wyoming", "WY", USA)
}

object Districts {
  import Countries._
  val DistrictOfColumbia = District("District of Columbia", "DC", USA)
}

object Territiories {
  import Countries._

  val AmericanSamoa = Territory("American Samoa", "AS", USA)
  val Guam = Territory("Guam", "GU", USA)
  val NorthernMarianaIslands = Territory("Northern Mariana Islands", "MP", USA)
  val PuertoRico = Territory("Puerto Rico", "PR", USA)
  val USVirginIslands = Territory("U.S. Virgin Islands", "VI", USA)
}

object Counties {
  import States._

  object FloridaCounties {
    val Escambia = County("Escambia", Florida)
    val SantaRosa = County("Santa Rosa", Florida)
    val Okaloosa = County("Okaloosa", Florida)
    val Walton = County("Walton", Florida)
    val Holmes = County("Holmes", Florida)
    val Washington = County("Washington", Florida)
    val Bay = County("Bay", Florida)
    val Jackson = County("Jackson", Florida)
    val Calhoun = County("Calhoun", Florida)
  }

  object IdahoCounties {
    val Boundary = County("Boundary", Idaho)
    val Bonner = County("Bonner", Idaho)
    val Kootenai = County("Kootenai", Idaho)
    val Shoshone = County("Shoshone", Idaho)
    val Benewah = County("Benewah", Idaho)
    val Latah = County("Latah", Idaho)
    val Clearwater = County("Clearwater", Idaho)
    val NezPerce = County("Nez Perce", Idaho)
    val Lewis = County("Lewis", Idaho)
    // fixme, parts of Idaho county differ.
  }
}

object TimeZones {
  val HST = DateTimeZone.forOffsetHours(-10)
  val AKST = DateTimeZone.forOffsetHours(-9)
  val AKDT = DateTimeZone.forOffsetHours(-8)
  val EST = DateTimeZone.forOffsetHours(-5)
  val EDT = DateTimeZone.forOffsetHours(-4)
  val CST = DateTimeZone.forOffsetHours(-6)
  val CDT = DateTimeZone.forOffsetHours(-5)
  val MST = DateTimeZone.forOffsetHours(-7)
  val MDT = DateTimeZone.forOffsetHours(-6)
  val PST = DateTimeZone.forOffsetHours(-8)
  val PDT = DateTimeZone.forOffsetHours(-7)

  import States._
  import Counties._

  val EasternDaylight = DaylightSavingsTimeZone(EST, EDT, ESTSummerTime)
  val CentralDaylight = DaylightSavingsTimeZone(CST, CDT, CSTSummerTime)
  val PacificDaylight = DaylightSavingsTimeZone(PST, PDT, PSTSummerTime)
  val MountainDaylight = DaylightSavingsTimeZone(MST, MDT, MSTSummerTime)

  val ByCounty = Map(
    FloridaCounties.Escambia -> CentralDaylight,
    FloridaCounties.SantaRosa -> CentralDaylight,
    FloridaCounties.Okaloosa -> CentralDaylight,
    FloridaCounties.Walton -> CentralDaylight,
    FloridaCounties.Holmes -> CentralDaylight,
    FloridaCounties.Bay -> CentralDaylight,
    FloridaCounties.Jackson -> CentralDaylight,
    FloridaCounties.Calhoun -> CentralDaylight,
    IdahoCounties.Benewah -> PacificDaylight,
    IdahoCounties.Bonner -> PacificDaylight,
    IdahoCounties.Boundary -> PacificDaylight,
    IdahoCounties.Clearwater -> PacificDaylight,
    IdahoCounties.Kootenai -> PacificDaylight,
    IdahoCounties.Latah -> PacificDaylight,
    IdahoCounties.Lewis -> PacificDaylight,
    IdahoCounties.NezPerce -> PacificDaylight,
    IdahoCounties.Shoshone -> PacificDaylight
  )

  val ByState = Map(
    Alabama -> EasternDaylight, // FIXME, except Western parts
    Alaska -> DaylightSavingsTimeZone(AKST, AKDT, new USSummerTime(AKST)),
    Arizona -> SingleTimeZone(MST),
    Arkansas -> CentralDaylight,
    California -> PacificDaylight,
    Colorado -> MountainDaylight,
    Connecticut -> EasternDaylight,
    Delaware -> EasternDaylight,
    Florida -> EasternDaylight,
    Georgia -> EasternDaylight,
    Hawaii -> SingleTimeZone(HST),
    Idaho -> MountainDaylight,
    Illinois -> CentralDaylight,
    Indiana -> EasternDaylight, // FIXME, except corners
    Iowa -> CentralDaylight,
    Kansas -> CentralDaylight, // FIXME, except Western counties
    Kentucky -> EasternDaylight, // FIXME, except Western half
    Louisiana -> CentralDaylight,
    Maine -> EasternDaylight,
    Maryland -> EasternDaylight,
    Massachusetts -> EasternDaylight,
    Michigan -> EasternDaylight, // FIXME, except Wisconsin border
    Minnesota -> CentralDaylight,
    Mississippi -> CentralDaylight,
    Missouri -> CentralDaylight,
    Montana -> MountainDaylight,
    Nebraska -> CentralDaylight, // FIXME, except West parts
    Nevada -> PacificDaylight, // FIXME, except eastern parts
    NewJersey -> EasternDaylight,
    NewMexico -> MountainDaylight,
    NewYork -> EasternDaylight,
    NorthCarolina -> EasternDaylight,
    NorthDakota -> CentralDaylight, // FIXME, except west parts
    Ohio -> EasternDaylight,
    Oklahoma -> CentralDaylight, // FIXME, but some unofficial parts
    Oregon -> PacificDaylight, // FIXME, except eastern parts
    Pennsylvania -> EasternDaylight,
    RhodeIsland -> EasternDaylight,
    SouthCarolina -> EasternDaylight,
    SouthDakota -> CentralDaylight, // FIXME, except west half
    Tennessee -> CentralDaylight, // FIXME, except east side
    Texas -> CentralDaylight, // FIXME, except west parts
    Utah -> MountainDaylight,
    Vermont -> EasternDaylight,
    Virginia -> EasternDaylight,
    Washington -> PacificDaylight,
    WestVirginia -> EasternDaylight,
    Wisconsin -> CentralDaylight,
    Wyoming -> MountainDaylight
  )
}