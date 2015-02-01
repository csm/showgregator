package org.showgregator.service

import java.util.UUID

import com.datastax.driver.core.Cluster
import com.twitter.finatra.test.MockApp
import com.twitter.util.Await
import com.websudos.phantom.testing.CassandraTest
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import org.showgregator.service.controller.LoginController
import org.showgregator.service.model.{TransientUser, TransientUserRecord, Connector}
import org.showgregator.service.session.DefaultSessionStore

/**
 * Created by cmarshall on 1/31/15.
 */
class TransientUserSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  import scala.concurrent.ExecutionContext.Implicits.global

  val keyspace = "showgregator_test_" + UUID.randomUUID().toString.split("-").head
  InitTables.run(keyspace)
  println(s"keyspace $keyspace created")
  val cassandra = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session = cassandra.connect(keyspace)
  implicit val sessionStore = new DefaultSessionStore
  val loginApp = MockApp(new LoginController())

  "fetching invalid token" should "return an error" in {
    val response = loginApp.get("/landing/350F7510-3B5D-4702-8F82-DE95E1854466")
    response.status should be (HttpResponseStatus.BAD_REQUEST)
  }

  "fetching invalid URL" should "return an error" in {
    val response = loginApp.get("/landing/not-an-actual-uuid")
    response.status should be (HttpResponseStatus.BAD_REQUEST)
  }

  "fetching a real token" should "redirect me" in {
    val userId = UUID.fromString("85CED1C9-2600-471F-B0CA-0A4D377BF638")
    val transientId = UUID.fromString("78677CA2-E651-44B3-B81D-95CE76DFBC5A")
    val create = TransientUser.insertUser(TransientUser("user@transient.com", userId, transientId))
    Await.result(create)
    val response = loginApp.get(s"/landing/$transientId?then=/someplace/new")
    response.status should be (HttpResponseStatus.FOUND)
    response.getHeader("location") should be ("/someplace/new")
  }

  override protected def afterAll(): Unit = {
    session.execute(s"DROP KEYSPACE $keyspace;")
    println(s"keyspace $keyspace dropped")
    super.afterAll()
  }
}
