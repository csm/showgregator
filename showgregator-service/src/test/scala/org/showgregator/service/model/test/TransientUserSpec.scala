package org.showgregator.service.model.test

import org.scalatest.FlatSpec
import com.websudos.phantom.testing.CassandraTest
import org.showgregator.service.model.{TransientUserRecord, TransientUser, Connector}
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/26/15
 * Time: 10:03 PM
 * To change this template use File | Settings | File Templates.
 */
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

  }
}
