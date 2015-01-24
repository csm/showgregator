package org.showgregator.service.model.test

import com.websudos.phantom.testing.{BaseTest, CassandraTest}
import java.util.UUID
import org.showgregator.service.model.{Permissions, Event, Connector, EventRecord}
import scala.concurrent.Await
import scala.concurrent.duration._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{ConfigMap, FlatSpec}
import org.joda.time.DateTime

@RunWith(classOf[JUnitRunner])
class EventSpec extends FlatSpec with CassandraTest with Connector {
  override val keySpace = "showgregator_test_eventSpec"

  "select absent event" should "return none" in {
    val id = new UUID(0, 0)
    val f = EventRecord.getById(id)
    val result = Await.result(f, 5.seconds)
    result.isEmpty should be (true)
  }

  "insert and select by id" should "return the same event" in {
    val id = UUID.fromString("1029EDF2-1580-4432-A627-26CB1B46513E")
    val venue = new UUID(0, 0)
    val event = Event(id, DateTime.now(), "Test Events and the Data Models",
      venue, None, "Riotous show where data is written and read from a database!",
      Map("*" -> (Permissions.Read | Permissions.Write)))
    Await.result(EventRecord.insertEvent(event), 5.seconds)
    val event2 = Await.result(EventRecord.getById(id), 5.seconds)
    event2.isDefined should be (true)
    event2.get should be (event)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(EventRecord.create.future(), 5.seconds)
  }
}
