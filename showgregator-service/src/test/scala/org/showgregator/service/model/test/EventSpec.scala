package org.showgregator.service.model.test

import com.websudos.phantom.testing.{BaseTest, CassandraTest}
import java.util.UUID
import org.showgregator.service.model.EventRecord
import scala.concurrent.Await
import scala.concurrent.duration._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EventSpec extends BaseTest {
  "select absent event" should "return none" in {
    val id = new UUID(0, 0)
    val f = EventRecord.getById(id)
    val result = Await.result(f, 5.seconds)
    result.isEmpty should be (true)
  }
}
