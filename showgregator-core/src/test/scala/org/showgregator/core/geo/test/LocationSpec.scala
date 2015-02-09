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
    val future = Location.findByGeolocation(36.971364, -122.025604)
    val result = Await.result(future, 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Santa Cruz")
    result.get.state should be (States.California)
  }

  "look up the Fillmore" should "return San Francisco" in {
    val future = Location.findByGeolocation(37.784004, -122.433133)
    val result = Await.result(future, 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("San Francisco")
    result.get.state should be (States.California)
  }

  // From here on I'm relying on the Internet for interesting/famous venues.
  // You are free to tell me if I'm wrong.

  "look up The Showbox" should "return Seattle" in {
    val result = Await.result(Location.findByGeolocation(47.608508, -122.339484), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("King")
    result.get.state should be (States.Washington)
  }

  "look up Mississippi Studios" should "return Oregon" in {
    val result = Await.result(Location.findByGeolocation(45.551427, -122.675843), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Multnomah")
    result.get.state should be (States.Oregon)
  }

  "look up the Underground" should "return Nevada" in {
    val result = Await.result(Location.findByGeolocation(39.531688, -119.806599), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Washoe")
    result.get.state should be (States.Nevada)
  }

  "look up Club Red & The Red Owl" should "return Arizona" in {
    val result = Await.result(Location.findByGeolocation(33.423169, -111.859360), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Maricopa")
    result.get.state should be (States.Arizona)
  }

  "look up KiMo Theater" should "return New Mexico" in {
    val result = Await.result(Location.findByGeolocation(35.084987, -106.652588), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Bernalillo")
    result.get.state should be (States.NewMexico)
  }

  "look up Red Rocks Amphitheatre" should "return Colorado" in {
    val result = Await.result(Location.findByGeolocation(39.665619, -105.205165), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Jefferson")
    result.get.state should be (States.Colorado)
  }

  "look up Idaho Center Arena" should "return Idaho" in {
    val result = Await.result(Location.findByGeolocation(43.608056, -116.513152), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Canyon")
    result.get.state should be (States.Idaho)
  }

  "look up Stubbs BBQ" should "return Texas" in {
    val result = Await.result(Location.findByGeolocation(30.268490, -97.736156), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Travis")
    result.get.state should be (States.Texas)
  }

  "look up the Bowery Ballroom" should "return New York" in {
    val result = Await.result(Location.findByGeolocation(40.720406, -73.993358), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("New York")
    result.get.state should be (States.NewYork)
  }

  "look up the Brighton Music Hall" should "return Massachusetts" in {
    val result = Await.result(Location.findByGeolocation(42.352873, -71.132590), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Suffolk")
    result.get.state should be (States.Massachusetts)
  }

  "look up the Benedum Center for the Performing Arts" should "return Pennsylvania" in {
    val result = Await.result(Location.findByGeolocation(40.442985, -80.000061), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Allegheny")
    result.get.state should be (States.Pennsylvania)
  }

  "look up Schubas Tavern" should "return Illinois" in {
    val result = Await.result(Location.findByGeolocation(41.939645, -87.663358), 1.minute)
    result shouldBe 'isDefined
    result.get.name should be ("Cook")
    result.get.state should be (States.Illinois)
  }
}
