package org.showgregator.core.geo

import java.io.File

import org.joda.time.{DateTime, DateTimeZone}

import scala.io.Source
import scala.util.parsing.json.{JSONObject, JSONArray, JSON}

import reflect.runtime.universe._
import reflect.runtime.currentMirror

object USLocales {

  case class Country(name: String, abbrev: String)
  case class City(name: String, county: County)
  case class County(name: String, state: State)
  case class State(name: String, abbrev: String, fipsCode: String, country: Country)
  case class District(name: String, abbrev: String, country: Country)
  case class Territory(name: String, abbrev: String, country: Country)

  object Countries {
    val USA = Country("The United States of America", "US")
  }

  object States {

    import Countries._

    val Alabama = State("Alabama", "AL", "01", USA)
    val Alaska = State("Alaska", "AK", "02", USA)
    val Arizona = State("Arizona", "AZ", "04", USA)
    val Arkansas = State("Arkansas", "AR", "05", USA)
    val California = State("California", "CA", "06", USA)
    val Colorado = State("Colorado", "CO", "08", USA)
    val Connecticut = State("Connecticut", "CT", "09", USA)
    val DistrictOfColumbia = State("District of Columbia", "DC", "11", USA)
    val Delaware = State("Delaware", "DE", "10", USA)
    val Florida = State("Florida", "FL", "12", USA)
    val Georgia = State("Georgia", "GA", "13", USA)
    val Hawaii = State("Hawaii", "HI", "15", USA)
    val Idaho = State("Idaho", "ID", "16", USA)
    val Illinois = State("Illinois", "IL", "17", USA)
    val Indiana = State("Indiana", "IN", "18", USA)
    val Iowa = State("Iowa", "IA", "19", USA)
    val Kansas = State("Kansas", "KS", "20", USA)
    val Kentucky = State("Kentucky", "KY", "21", USA)
    val Louisiana = State("Louisiana", "LA", "22", USA)
    val Maine = State("Maine", "ME", "23", USA)
    val Maryland = State("Maryland", "MD", "24", USA)
    val Massachusetts = State("Massachusetts", "MA", "25", USA)
    val Michigan = State("Michigan", "MI", "26", USA)
    val Minnesota = State("Minnesota", "MN", "27", USA)
    val Mississippi = State("Mississippi", "MS", "28", USA)
    val Missouri = State("Missouri", "MO", "29", USA)
    val Montana = State("Montana", "MT", "30", USA)
    val Nebraska = State("Nebraska", "NE", "31", USA)
    val Nevada = State("Nevada", "NV", "32", USA)
    val NewHampshire = State("New Hampshire", "NH", "33", USA)
    val NewJersey = State("New Jersey", "NJ", "34", USA)
    val NewMexico = State("New Mexico", "NM", "35", USA)
    val NewYork = State("New York", "NY", "36", USA)
    val NorthCarolina = State("North Carolina", "NC", "37", USA)
    val NorthDakota = State("North Dakota", "ND", "38", USA)
    val Ohio = State("Ohio", "OH", "39", USA)
    val Oklahoma = State("Oklahoma", "OK", "40", USA)
    val Oregon = State("Oregon", "OR", "41", USA)
    val Pennsylvania = State("Pennsylvania", "PA", "42", USA)
    val RhodeIsland = State("Rhode Island", "RI", "44", USA)
    val SouthCarolina = State("South Carolina", "SC", "45", USA)
    val SouthDakota = State("South Dakota", "SD", "46", USA)
    val Tennessee = State("Tennessee", "TN", "47", USA)
    val Texas = State("Texas", "TX", "48", USA)
    val Utah = State("Utah", "UT", "49", USA)
    val Vermont = State("Vermont", "VT", "50", USA)
    val Virginia = State("Virginia", "VA", "51", USA)
    val Washington = State("Washington", "WA", "53", USA)
    val WestVirginia = State("West Virginia", "WV", "54", USA)
    val Wisconsin = State("Wisconsin", "WI", "55", USA)
    val Wyoming = State("Wyoming", "WY", "56", USA)

    def forAbbrev(abbrev: String): Option[State] = {
      allStates().find(s => s.abbrev.equalsIgnoreCase(abbrev))
    }

    def forName(name: String): Option[State] = {
      allStates().find(s => s.name.equals(name))
    }

    def forFipsCode(code: String): Option[State] = {
      allStates().find(s => s.fipsCode.equals(code))
    }

    def allStates():Seq[State] = {
      val r = currentMirror.reflect(States)
      r.symbol.typeSignature.members.toStream
        .collect { case s: TermSymbol if !s.isMethod => r.reflectField(s) }
        .map(_.get)
        .filter(_.isInstanceOf[State])
        .map(_.asInstanceOf[State])
    }
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

    object IndianaCounties {
      val Jasper = County("Jasper", Indiana)
      val Lake = County("Lake", Indiana)
      val LaPorte = County("LaPorte", Indiana)
      val Newton = County("Newton", Indiana)
      val Porter = County("Porter", Indiana)
      val Starke = County("Starke", Indiana)
      val Gibson = County("Gibson", Indiana)
      val Perry = County("Perry", Indiana)
      val Posey = County("Posey", Indiana)
      val Spencer = County("Spencer", Indiana)
      val Vanderburgh = County("Vanderhurgh", Indiana)
      val Warrick = County("Warrick", Indiana)
    }

    object KansasCounties {
      val Greely = County("Greely", Kansas)
      val Hamilton = County("Hamilton", Kansas)
      val Sherman = County("Sherman", Kansas)
      val Wallace = County("Wallack", Kansas)
    }

    object KentuckyCounties {
      val Adair = County("Adair", Kentucky)
      val Allen = County("Allen", Kentucky)
      val Ballard = County("Ballard", Kentucky)
      val Barren = County("Barren", Kentucky)
      val Breckinridge = County("Breckinridge", Kentucky)
      val Butler = County("Butler", Kentucky)
      val Caldwell = County("Caldwell", Kentucky)
      val Calloway = County("Calloway", Kentucky)
      val Carlisle = County("Carlisle", Kentucky)
      val Christian = County("Christian", Kentucky)
      val Clinton = County("Clinton", Kentucky)
      val Crittenden = County("Crittenden", Kentucky)
      val Cumberland = County("Cumberland", Kentucky)
      val Daviess = County("Daviess", Kentucky)
      val Edmonson = County("Edmonson", Kentucky)
      val Fulton = County("Fulton", Kentucky)
      val Graves = County("Graves", Kentucky)
      val Grayson = County("Grayson", Kentucky)
      val Green = County("Green", Kentucky)
      val Hancock = County("Hancock", Kentucky)
      val Hart = County("Hart", Kentucky)
      val Henderson = County("Henderson", Kentucky)
      val Hickman = County("Hickman", Kentucky)
      val Hopkins = County("Hopkins", Kentucky)
      val Livingston = County("Livingston", Kentucky)
      val Logan = County("Logan", Kentucky)
      val McCracken = County("McCracken", Kentucky)
      val McLean = County("McLean", Kentucky)
      val Marshall = County("Marshall", Kentucky)
      val Metcalfe = County("Metcalfe", Kentucky)
      val Monroe = County("Monroe", Kentucky)
      val Muhlenberg = County("Muhlenberg", Kentucky)
      val Ohio = County("Ohio", Kentucky)
      val Russell = County("Russell", Kentucky)
      val Simpson = County("Simpson", Kentucky)
      val Todd = County("Todd", Kentucky)
      val Trigg = County("Trigg", Kentucky)
      val Union = County("Union", Kentucky)
      val Warren = County("Warren", Kentucky)
      val Webster = County("Webster", Kentucky)
    }

    object MichiganCounties {
      val Gogebic = County("Gogebic", Michigan)
      val Iron = County("Iron", Michigan)
      val Dickinson = County("Dickinson", Michigan)
      val Menominee = County("Menominee", Michigan)
    }

    object NebraskaCounties {
      val Arthur = County("Arthur", Nebraska)
      val Banner = County("Banner", Nebraska)
      val BoxButte = County("Box Butte", Nebraska)
      val Chase = County("Chase", Nebraska)
      val Cheyenne = County("Cheyenne", Nebraska)
      val Dawes = County("Dawes", Nebraska)
      val Deuel = County("Deuel", Nebraska)
      val Dundy = County("Dundy", Nebraska)
      val Garden = County("Garden", Nebraska)
      val Grant = County("Grant", Nebraska)
      val Hooker = County("Hooker", Nebraska)
      val Keith = County("Keith", Nebraska)
      val Kimball = County("Kimball", Nebraska)
      val Morrill = County("Morrill", Nebraska)
      val Perkins = County("Perkins", Nebraska)
      val ScottsBluff = County("Scotts Bluff", Nebraska)
      val Sheridan = County("Sheridan", Nebraska)
      val Sioux = County("Sioux", Nebraska)
    }

    object NorthDakotaCounties {
      val Adams = County("Adams", NorthDakota)
      val Billings = County("Billings", NorthDakota)
      val Bowman = County("Bowman", NorthDakota)
      val Dunn = County("Dunn", NorthDakota)
      val GoldenValley = County("Golden Valley", NorthDakota)
      val Grant = County("Grant", NorthDakota)
      val Hettinger = County("Hettinger", NorthDakota)
      val Sioux = County("Sioux", NorthDakota)
      val Slope = County("Slope", NorthDakota)
      val Stark = County("Stark", NorthDakota)
    }

    object OregonCounties {
      val Malheur = County("Malheur", Oregon)
    }

    object TennesseeCounties {
      val Anderson = County("Anderson", Tennessee)
      val Blount = County("Blount", Tennessee)
      val Bradley = County("Bradley", Tennessee)
      val Campbell = County("Campbell", Tennessee)
      val Carter = County("Carter", Tennessee)
      val Claiborne = County("Claiborne", Tennessee)
      val Cocke = County("Cocke", Tennessee)
      val Grainger = County("Grainger", Tennessee)
      val Greene = County("Greene", Tennessee)
      val Hamblen = County("Hamblen", Tennessee)
      val Hamilton = County("Hamilton", Tennessee)
      val Hancock = County("Hancock", Tennessee)
      val Hawkins = County("Hawkins", Tennessee)
      val Jefferson = County("Jefferson", Tennessee)
      val Johnson = County("Johnson", Tennessee)
      val Knox = County("Knox", Tennessee)
      val Loudon = County("Loudon", Tennessee)
      val McMinn = County("McMinn", Tennessee)
      val Meigs = County("Meigs", Tennessee)
      val Monroe = County("Monroe", Tennessee)
      val Morgan = County("Morgan", Tennessee)
      val Polk = County("Polk", Tennessee)
      val Rhea = County("Rhea", Tennessee)
      val Roane = County("Roane", Tennessee)
      val Scott = County("Scott", Tennessee)
      val Sevier = County("Sevier", Tennessee)
      val Sullivan = County("Sullivan", Tennessee)
      val Unicoi = County("Unicoi", Tennessee)
      val Union = County("Union", Tennessee)
      val Washington = County("Washington", Tennessee)
    }

    object SouthDakotaCounties {
      val Bennett = County("Bennett", SouthDakota)
      val Butte = County("Butte", SouthDakota)
      val Corson = County("Corson", SouthDakota)
      val Custer = County("Custer", SouthDakota)
      val Dewey = County("Dewey", SouthDakota)
      val FallRiver = County("Fall River", SouthDakota)
      val Haakon = County("Haakon", SouthDakota)
      val Harding = County("Harding", SouthDakota)
      val Jackson = County("Jackson", SouthDakota)
      val Lawrence = County("Lawrence", SouthDakota)
      val Meade = County("Meade", SouthDakota)
      val Pennington = County("Pennington", SouthDakota)
      val Perkins = County("Perkins", SouthDakota)
      val Shannon = County("Shannon", SouthDakota)
      val Stanley = County("Stanley", SouthDakota) // fixme, except east part
      val Ziebach = County("Zieback", SouthDakota)
    }

    object TexasCounties {
      val ElPaso = County("El Paso", Texas)
      val Hudspeth = County("Hudspeth", Texas)
    }
  }

  object Cities {
    def loadCityData(dir: File): Seq[City] = {
      States.allStates().map(s => {
        val input = Source.fromFile(new File(dir, s"${s.abbrev}.json")).mkString
        JSON.parseRaw(input) match {
          case Some(a: JSONArray) => a.list.map {
            case obj: JSONObject => try {
              Some(City(obj.obj.get("name").get.asInstanceOf[String],
                County(obj.obj.get("county_name").get.asInstanceOf[String],
                  States.forAbbrev(obj.obj.get("state_abbreviation").get.asInstanceOf[String]).get)))
            } catch {
              case t:Throwable => None
            }

            case _ => None
          }.toList

          case _ => List()
        }
      }).flatten.filter(_.isDefined).map(_.get)
    }
  }

  object TimeZones {

    import States._
    import Counties._

    val EasternDaylight = "America/New_York"
    val CentralDaylight = "America/Chicago"
    val PacificDaylight = "America/Los_Angeles"
    val MountainDaylight = "America/Denver"

    def findZoneId(city: City):Option[String] = {
      ByCity.get(city) orElse ByCounty.get(city.county) orElse ByState.get(city.county.state)
    }

    def findZoneForCounty(county: County): Option[String] = {
      ByCounty.get(county) orElse ByState.get(county.state)
    }

    val ByCity = Map[City, String]()

    val ByCounty = Map[County, String](
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
      IdahoCounties.Shoshone -> PacificDaylight,

      IndianaCounties.Gibson -> CentralDaylight,
      IndianaCounties.Jasper -> CentralDaylight,
      IndianaCounties.Lake -> CentralDaylight,
      IndianaCounties.LaPorte -> CentralDaylight,
      IndianaCounties.Newton -> CentralDaylight,
      IndianaCounties.Perry -> "America/Indiana/Tell_City",
      IndianaCounties.Porter -> "America/Indiana/Valparaiso",
      IndianaCounties.Posey -> CentralDaylight,
      IndianaCounties.Spencer -> CentralDaylight,
      IndianaCounties.Starke -> "America/Indiana/Knox",
      IndianaCounties.Vanderburgh -> CentralDaylight,
      IndianaCounties.Warrick -> CentralDaylight,

      KansasCounties.Greely -> MountainDaylight,
      KansasCounties.Hamilton -> MountainDaylight,
      KansasCounties.Sherman -> MountainDaylight,
      KansasCounties.Wallace -> MountainDaylight,

      KentuckyCounties.Adair -> CentralDaylight,
      KentuckyCounties.Allen -> CentralDaylight,
      KentuckyCounties.Ballard -> CentralDaylight,
      KentuckyCounties.Barren -> CentralDaylight,
      KentuckyCounties.Breckinridge -> CentralDaylight,
      KentuckyCounties.Butler -> CentralDaylight,
      KentuckyCounties.Caldwell -> CentralDaylight,
      KentuckyCounties.Calloway -> CentralDaylight,
      KentuckyCounties.Carlisle -> CentralDaylight,
      KentuckyCounties.Christian -> CentralDaylight,
      KentuckyCounties.Clinton -> CentralDaylight,
      KentuckyCounties.Crittenden -> CentralDaylight,
      KentuckyCounties.Cumberland -> CentralDaylight,
      KentuckyCounties.Daviess -> CentralDaylight,
      KentuckyCounties.Edmonson -> CentralDaylight,
      KentuckyCounties.Fulton -> CentralDaylight,
      KentuckyCounties.Graves -> CentralDaylight,
      KentuckyCounties.Grayson -> CentralDaylight,
      KentuckyCounties.Green -> CentralDaylight,
      KentuckyCounties.Hancock -> CentralDaylight,
      KentuckyCounties.Hart -> CentralDaylight,
      KentuckyCounties.Henderson -> CentralDaylight,
      KentuckyCounties.Hickman -> CentralDaylight,
      KentuckyCounties.Hopkins -> CentralDaylight,
      KentuckyCounties.Livingston -> CentralDaylight,
      KentuckyCounties.Logan -> CentralDaylight,
      KentuckyCounties.Logan -> CentralDaylight,
      KentuckyCounties.McCracken -> CentralDaylight,
      KentuckyCounties.McLean -> CentralDaylight,
      KentuckyCounties.Marshall -> CentralDaylight,
      KentuckyCounties.Metcalfe -> CentralDaylight,
      KentuckyCounties.Monroe -> CentralDaylight,
      KentuckyCounties.Muhlenberg -> CentralDaylight,
      KentuckyCounties.Ohio -> CentralDaylight,
      KentuckyCounties.Russell -> CentralDaylight,
      KentuckyCounties.Simpson -> CentralDaylight,
      KentuckyCounties.Todd -> CentralDaylight,
      KentuckyCounties.Trigg -> CentralDaylight,
      KentuckyCounties.Union -> CentralDaylight,
      KentuckyCounties.Warren -> CentralDaylight,
      KentuckyCounties.Webster -> CentralDaylight,

      MichiganCounties.Dickinson -> "America/Menominee",
      MichiganCounties.Gogebic -> "America/Menominee",
      MichiganCounties.Iron -> "America/Menominee",
      MichiganCounties.Menominee -> "America/Menominee",

      NebraskaCounties.Arthur -> MountainDaylight,
      NebraskaCounties.Banner -> MountainDaylight,
      NebraskaCounties.BoxButte -> MountainDaylight,
      NebraskaCounties.Chase -> MountainDaylight,
      NebraskaCounties.Cheyenne -> MountainDaylight,
      NebraskaCounties.Dawes -> MountainDaylight,
      NebraskaCounties.Deuel -> MountainDaylight,
      NebraskaCounties.Dundy -> MountainDaylight,
      NebraskaCounties.Garden -> MountainDaylight,
      NebraskaCounties.Grant -> MountainDaylight,
      NebraskaCounties.Hooker -> MountainDaylight,
      NebraskaCounties.Keith -> MountainDaylight,
      NebraskaCounties.Kimball -> MountainDaylight,
      NebraskaCounties.Morrill -> MountainDaylight,
      NebraskaCounties.Perkins -> MountainDaylight,
      NebraskaCounties.ScottsBluff -> MountainDaylight,
      NebraskaCounties.Sheridan -> MountainDaylight,
      NebraskaCounties.Sioux -> MountainDaylight,

      NorthDakotaCounties.Adams -> MountainDaylight,
      NorthDakotaCounties.Billings -> MountainDaylight,
      NorthDakotaCounties.Bowman -> MountainDaylight,
      NorthDakotaCounties.Dunn -> MountainDaylight,
      NorthDakotaCounties.GoldenValley -> MountainDaylight,
      NorthDakotaCounties.Grant -> MountainDaylight,
      NorthDakotaCounties.Hettinger -> MountainDaylight,
      NorthDakotaCounties.Sioux -> MountainDaylight,
      NorthDakotaCounties.Slope -> MountainDaylight,
      NorthDakotaCounties.Stark -> MountainDaylight,

      OregonCounties.Malheur -> "America/Boise",

      TennesseeCounties.Anderson -> EasternDaylight,
      TennesseeCounties.Blount -> EasternDaylight,
      TennesseeCounties.Bradley -> EasternDaylight,
      TennesseeCounties.Campbell -> EasternDaylight,
      TennesseeCounties.Carter -> EasternDaylight,
      TennesseeCounties.Claiborne -> EasternDaylight,
      TennesseeCounties.Cocke -> EasternDaylight,
      TennesseeCounties.Grainger -> EasternDaylight,
      TennesseeCounties.Greene -> EasternDaylight,
      TennesseeCounties.Hamblen -> EasternDaylight,
      TennesseeCounties.Hamilton -> EasternDaylight,
      TennesseeCounties.Hancock -> EasternDaylight,
      TennesseeCounties.Hawkins -> EasternDaylight,
      TennesseeCounties.Jefferson -> EasternDaylight,
      TennesseeCounties.Johnson -> EasternDaylight,
      TennesseeCounties.Knox -> EasternDaylight,
      TennesseeCounties.Loudon -> EasternDaylight,
      TennesseeCounties.McMinn -> EasternDaylight,
      TennesseeCounties.Meigs -> EasternDaylight,
      TennesseeCounties.Monroe -> EasternDaylight,
      TennesseeCounties.Morgan -> EasternDaylight,
      TennesseeCounties.Polk -> EasternDaylight,
      TennesseeCounties.Rhea -> EasternDaylight,
      TennesseeCounties.Roane -> EasternDaylight,
      TennesseeCounties.Scott -> EasternDaylight,
      TennesseeCounties.Sevier -> EasternDaylight,
      TennesseeCounties.Sullivan -> EasternDaylight,
      TennesseeCounties.Unicoi -> EasternDaylight,
      TennesseeCounties.Union -> EasternDaylight,
      TennesseeCounties.Washington -> EasternDaylight,

      SouthDakotaCounties.Bennett -> MountainDaylight,
      SouthDakotaCounties.Butte -> MountainDaylight,
      SouthDakotaCounties.Corson -> MountainDaylight,
      SouthDakotaCounties.Custer -> MountainDaylight,
      SouthDakotaCounties.Dewey -> MountainDaylight,
      SouthDakotaCounties.FallRiver -> MountainDaylight,
      SouthDakotaCounties.Haakon -> MountainDaylight,
      SouthDakotaCounties.Harding -> MountainDaylight,
      SouthDakotaCounties.Jackson -> MountainDaylight,
      SouthDakotaCounties.Lawrence -> MountainDaylight,
      SouthDakotaCounties.Meade -> MountainDaylight,
      SouthDakotaCounties.Pennington -> MountainDaylight,
      SouthDakotaCounties.Perkins -> MountainDaylight,
      SouthDakotaCounties.Shannon -> MountainDaylight,
      SouthDakotaCounties.Stanley -> MountainDaylight,
      SouthDakotaCounties.Ziebach -> MountainDaylight,

      TexasCounties.ElPaso -> MountainDaylight,
      TexasCounties.Hudspeth -> MountainDaylight
    )

    val ByState = Map[State, String](
      Alabama -> EasternDaylight, // FIXME, except Western parts
      Alaska -> "America/Anchorage",
      Arizona -> "America/Phoenix", // FIXME except Navajo nation
      Arkansas -> CentralDaylight,
      California -> PacificDaylight,
      Colorado -> MountainDaylight,
      Connecticut -> EasternDaylight,
      DistrictOfColumbia -> EasternDaylight,
      Delaware -> EasternDaylight,
      Florida -> EasternDaylight,
      Georgia -> EasternDaylight,
      Hawaii -> "Pacific/Honolulu",
      Idaho -> "America/Boise",
      Illinois -> CentralDaylight,
      Indiana -> "America/Indiana/Indianapolis",
      Iowa -> CentralDaylight,
      Kansas -> CentralDaylight,
      Kentucky -> EasternDaylight,
      Louisiana -> CentralDaylight,
      Maine -> EasternDaylight,
      Maryland -> EasternDaylight,
      Massachusetts -> EasternDaylight,
      Michigan -> "America/Detroit",
      Minnesota -> CentralDaylight,
      Mississippi -> CentralDaylight,
      Missouri -> CentralDaylight,
      Montana -> MountainDaylight,
      Nebraska -> CentralDaylight,
      Nevada -> PacificDaylight, // FIXME, except northern parts of Elko County
      NewJersey -> EasternDaylight,
      NewMexico -> MountainDaylight,
      NewYork -> EasternDaylight,
      NorthCarolina -> EasternDaylight,
      NorthDakota -> CentralDaylight,
      Ohio -> EasternDaylight,
      Oklahoma -> CentralDaylight, // FIXME, but some unofficial parts
      Oregon -> PacificDaylight,
      Pennsylvania -> EasternDaylight,
      RhodeIsland -> EasternDaylight,
      SouthCarolina -> EasternDaylight,
      SouthDakota -> CentralDaylight,
      Tennessee -> CentralDaylight,
      Texas -> CentralDaylight,
      Utah -> MountainDaylight,
      Vermont -> EasternDaylight,
      Virginia -> EasternDaylight,
      Washington -> PacificDaylight,
      WestVirginia -> EasternDaylight,
      Wisconsin -> CentralDaylight,
      Wyoming -> MountainDaylight
    )
  }
}