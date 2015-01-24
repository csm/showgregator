package org.showgregator.service.model.test

import org.scalatest.FlatSpec
import com.websudos.phantom.testing.CassandraTest
import org.showgregator.service.model.{EventInCalendar, EventInCalendarRecord, Connector}
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.UUID
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EventInCalendarSpec extends FlatSpec with CassandraTest with Connector {
  override val keySpace = "showgregator_test_eventsInCalendarSpec"

  "select absent calendar" should "return nothing" in {
    val id = new UUID(0, 0)
    val events = Await.result(EventInCalendarRecord.fetchForCalendar(id, DateTime.now().minusDays(1), DateTime.now().plusDays(1)), 5.seconds)
    events.isEmpty should be (true)
  }

  "select events in range" should "only return events in that range" in {
    val calendar = UUID.randomUUID()
    val now = DateTime.now()
    val events = List(1, 2, 3, 4, 5).map(days => {
      val e = EventInCalendar(calendar, UUID.randomUUID(), now.plusDays(days), s"Event $days days from now")
      Await.result(EventInCalendarRecord.insertEvent(e), 5.seconds)
      e
    })
    val start = now.plusDays(3).minusHours(12)
    val end = now.plusDays(3).plusHours(12)
    val events2 = Await.result(EventInCalendarRecord.fetchForCalendar(calendar, start, end), 5.seconds)
    events2.isEmpty should be (false)
    events2.size should be (1)
    events2.head should be (events(2))
  }
  
  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(EventInCalendarRecord.create.future(), 5.seconds)
  }
}
