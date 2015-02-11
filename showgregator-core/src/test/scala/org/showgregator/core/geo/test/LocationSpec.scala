package org.showgregator.core.geo.test

import org.scalatest.{Matchers, FlatSpec}
import org.showgregator.core.geo.USLocales.States
import org.showgregator.core.geo.Location
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by cmarshall on 2/8/15.
 */
class LocationSpec extends FlatSpec with Matchers {
  "look up the Catalyst" should "return Santa Cruz" in {
    val future = Location.findByGeolocation2(36.971364, -122.025604)
    val result = Await.result(future, 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Santa Cruz")
    result.get.county.name should be ("Santa Cruz")
    result.get.county.state should be (States.California)
  }

  "look up the Fillmore" should "return San Francisco" in {
    val future = Location.findByGeolocation2(37.784004, -122.433133)
    val result = Await.result(future, 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("San Francisco")
    result.get.county.name should be ("San Francisco")
    result.get.county.state should be (States.California)
  }

  // From here on I'm relying on the Internet for interesting/famous venues.
  // You are free to tell me if I'm wrong.

  "look up The Showbox" should "return Seattle WA" in {
    val result = Await.result(Location.findByGeolocation2(47.608508, -122.339484), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Seattle")
    result.get.county.name should be ("King")
    result.get.county.state should be (States.Washington)
  }

  "look up Mississippi Studios" should "return Portland OR" in {
    val result = Await.result(Location.findByGeolocation2(45.551427, -122.675843), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Portland")
    result.get.county.name should be ("Multnomah")
    result.get.county.state should be (States.Oregon)
  }

  "look up the Underground" should "return Reno, Nevada" in {
    val result = Await.result(Location.findByGeolocation2(39.531688, -119.806599), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Reno")
    result.get.county.name should be ("Washoe")
    result.get.county.state should be (States.Nevada)
  }

  "look up Club Red & The Red Owl" should "return Mesa, Arizona" in {
    val result = Await.result(Location.findByGeolocation2(33.423169, -111.859360), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Mesa")
    result.get.county.name should be ("Maricopa")
    result.get.county.state should be (States.Arizona)
  }

  "look up KiMo Theater" should "return Albuquerque, New Mexico" in {
    val result = Await.result(Location.findByGeolocation2(35.084987, -106.652588), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Albuquerque")
    result.get.county.name should be ("Bernalillo")
    result.get.county.state should be (States.NewMexico)
  }

  "look up Red Rocks Amphitheatre" should "return Morrison, Colorado" in {
    val result = Await.result(Location.findByGeolocation2(39.665619, -105.205165), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Morrison")
    result.get.county.name should be ("Jefferson")
    result.get.county.state should be (States.Colorado)
  }

  "look up Idaho Center Arena" should "return Nampa, Idaho" in {
    val result = Await.result(Location.findByGeolocation2(43.608056, -116.513152), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Nampa")
    result.get.county.name should be ("Canyon")
    result.get.county.state should be (States.Idaho)
  }

  "look up Stubbs BBQ" should "return Austin, Texas" in {
    val result = Await.result(Location.findByGeolocation2(30.268490, -97.736156), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Austin")
    result.get.county.name should be ("Travis")
    result.get.county.state should be (States.Texas)
  }

  "look up the Bowery Ballroom" should "return New York" in {
    val result = Await.result(Location.findByGeolocation2(40.720406, -73.993358), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("New York")
    result.get.county.name should be ("New York")
    result.get.county.state should be (States.NewYork)
  }

  "look up the Brighton Music Hall" should "return Cambridge, Massachusetts" in {
    val result = Await.result(Location.findByGeolocation2(42.352873, -71.132590), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Cambridge") // Might actually be Boston, but points are closer to Cambridge center
    result.get.county.name should be ("Middlesex")
    result.get.county.state should be (States.Massachusetts)
  }

  "look up the Benedum Center for the Performing Arts" should "return Pittsburgh, Pennsylvania" in {
    val result = Await.result(Location.findByGeolocation2(40.442985, -80.000061), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Pittsburgh")
    result.get.county.name should be ("Allegheny")
    result.get.county.state should be (States.Pennsylvania)
  }

  "look up Schubas Tavern" should "return Chicago, Illinois" in {
    val result = Await.result(Location.findByGeolocation2(41.939645, -87.663358), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Lincolnwood") // Actually is closer to Lincolnwood center than Chicago; should try to fix that.
    result.get.county.name should be ("Cook")
    result.get.county.state should be (States.Illinois)
  }
}
