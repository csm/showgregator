package org.showgregator.service.model.test

import org.showgregator.service.model.{Permissions, Calendar, CalendarRecord, Connector}
import com.websudos.phantom.testing.CassandraTest
import org.scalatest.{ConfigMap, FlatSpec}
import scala.concurrent.Await
import scala.concurrent.duration._
import java.util.UUID

class CalendarSpec extends FlatSpec with CassandraTest with Connector {
  override val keySpace = "showgregator_test_calendarSpec"

  "select absent calendar" should "return nothing" in {
    val id = new UUID(0, 0)
    val cal = Await.result(CalendarRecord.getById(id), 5.seconds)
    cal.isEmpty should be (true)
  }

  "insert and select the same id" should "return the same calendar" in {
    val cal = Calendar(UUID.fromString("5E6B1461-F8B7-4052-BC41-20E4D20E75F6"),
      "A Test Calendar", Map("*" -> Permissions.Read))
    Await.result(CalendarRecord.insertCalendar(cal), 5.seconds)
    val cal2 = Await.result(CalendarRecord.getById(cal.id), 5.seconds)
    cal2.isDefined should be (true)
    cal2.get should be (cal)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    session.execute(s"CREATE KEYSPACE $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};")
    Await.result(CalendarRecord.create.future(), 5.seconds)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    session.execute(s"DROP KEYSPACE $keySpace;")
  }
}
