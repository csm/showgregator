package org.showgregator.service.model.test

import java.util.UUID

import com.websudos.phantom.testing.CassandraTest
import org.joda.time.DateTime
import org.scalatest.FlatSpec
import org.showgregator.service.model.{Connector, Event, EventRecord, EventPermissions}

import com.twitter.util.Await
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit._

class EventSpec extends FlatSpec with CassandraTest with Connector {
  override val keySpace = "showgregator_test_eventSpec"

  "select absent event" should "return none" in {
    val id = new UUID(0, 0)
    val f = EventRecord.getById(id)
    val result = Await.result(f, Duration(5, SECONDS))
    result.isEmpty should be (true)
  }

  "insert and select by id" should "return the same event" in {
    val id = UUID.fromString("1029EDF2-1580-4432-A627-26CB1B46513E")
    val venue = new UUID(0, 0)
    val event = Event(id, DateTime.now(), "Test Events and the Data Models",
      venue, None, "Riotous show where data is written and read from a database!",
      Map(new UUID(0, 0) -> (EventPermissions.Read | EventPermissions.Edit)))
    Await.result(EventRecord.insertEvent(event), Duration(5, SECONDS))
    val event2 = Await.result(EventRecord.getById(id), Duration(5, SECONDS))
    event2.isDefined should be (true)
    event2.get should be (event)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    session.execute(s"CREATE KEYSPACE $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};")
    Await.result(EventRecord.create.execute(), Duration(5, SECONDS))
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    session.execute(s"DROP KEYSPACE $keySpace;")
  }
}
