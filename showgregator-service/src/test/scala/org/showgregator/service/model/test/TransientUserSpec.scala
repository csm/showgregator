package org.showgregator.service.model.test

import org.scalatest.FlatSpec
import com.websudos.phantom.testing.CassandraTest
import org.showgregator.service.model.{ReverseTransientUserRecord, TransientUserRecord, TransientUser, Connector}
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration._

class TransientUserSpec extends FlatSpec with CassandraTest with Connector {
  override val keySpace = "showgregator_test_transientUserSpec"

  "add and fetch" should "return the same value" in {
    val user = TransientUser("foo@bar.com", UUID.fromString("fd8f1c7b-0338-4e79-a1ef-ef6b2c5d1d2f"))
    Await.result(TransientUserRecord.insertUser(user), 1.minute)
    val user2 = Await.result(TransientUserRecord.forEmail("foo@bar.com"), 1.minute)
    user2.isDefined should be (true)
    user2.get should be (user)
  }

  "add and fetch reverse" should "return the same value" in {
    val user = TransientUser("foo@bar.com", UUID.fromString("5A41442F-A9BF-4371-8FA5-1D0B278C2E02"))
    Await.result(ReverseTransientUserRecord.insertUser(user), 1.minute)
    val user2 = Await.result(ReverseTransientUserRecord.forUuid(user.id), 1.minute)
    user2.isDefined should be (true)
    user2.get should be (user)
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    session.execute(s"CREATE KEYSPACE $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};")
    Await.result(TransientUserRecord.create.future(), 1.minute)
    Await.result(ReverseTransientUserRecord.create.future(), 1.minute)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    session.execute(s"DROP KEYSPACE $keySpace;")
  }
}
