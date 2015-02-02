package org.showgregator.service

import java.util.UUID

import com.datastax.driver.core.Cluster
import com.twitter.finatra.test.MockApp
import com.twitter.logging.{ConsoleHandler, Level, Logger}
import com.twitter.util.Await
import com.websudos.phantom.testing.CassandraTest
import org.jboss.netty.handler.codec.http.{Cookie, HttpResponseStatus}
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import org.showgregator.service.controller.{VerifyEmailController, UserController, LoginController}
import org.showgregator.service.model._
import org.showgregator.service.session.DefaultSessionStore

/**
 * Created by cmarshall on 1/31/15.
 */
class TransientUserSpec extends AbstractServiceSpec with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  /*val log = Logger("finatra")
  log.addHandler(new ConsoleHandler(level = Some(Level.ALL)))
  log.setLevel(Level.ALL)
  val keyspace = "showgregator_test_" + UUID.randomUUID().toString.split("-")(0)
  InitTables.run(keyspace)
  println(s"keyspace $keyspace created")
  val cassandra = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session = cassandra.connect(keyspace)
  implicit val sessionStore = new DefaultSessionStore*/
  val loginApp = MockApp(new LoginController())
  val userApp = MockApp(new UserController())
  val verifyApp = MockApp(new VerifyEmailController())

  "fetching invalid token" should "return an error" in {
    val response = loginApp.get("/landing/350F7510-3B5D-4702-8F82-DE95E1854466")
    response.status should be (BAD_REQUEST)
  }

  "fetching invalid URL" should "return an error" in {
    val response = loginApp.get("/landing/not-an-actual-uuid")
    response.status should be (BAD_REQUEST)
  }

  "fetching a real token" should "redirect me" in {
    val userId = UUID.fromString("85CED1C9-2600-471F-B0CA-0A4D377BF638")
    val transientId = UUID.fromString("78677CA2-E651-44B3-B81D-95CE76DFBC5A")
    val create = TransientUser.insertUser(TransientUser("user@transient.com", userId, transientId))
    Await.result(create)
    val response = loginApp.get(s"/landing/$transientId?then=/someplace/new")
    response.status should be (FOUND)
    response.getHeader("location") should be ("/someplace/new")
    response.getHeader("set-cookie").indexOf("SessionId=") should be >= 0
  }

  "registering a transient user" should "create a pending user" in {
    val userId = UUID.fromString("E356ED44-C61C-4FA8-B1E3-C086844D08B3")
    val transientId = UUID.fromString("CFD62A55-6C0B-4F20-BB5F-2E7D27AAA618")
    val create = TransientUser.insertUser(TransientUser("user2@transient.com", userId, transientId))
    Await.result(create)
    val response = loginApp.get(s"/landing/$transientId?then=/register")
    response.status should be (FOUND)
    response.getHeader("location") should be ("/register")
    response.getHeader("set-cookie").indexOf("SessionId=") should be >= 0
    val sessionId = response
      .getHeader("set-cookie").split(";").map(_.trim().split("=")).find(a => a(0).equals("SessionId")).get(1)
    val registerResponse = userApp.post("/register", Map("email" -> "user2@transient.com",
      "password" -> "ch@ng3it!", "confirm" -> "ch@ng3it!"), Map("Cookie" -> s"SessionId=$sessionId"))
    registerResponse.status should be (OK)
    val reversePending = Await.result(ReversePendingUserRecord.getByEmail("user2@transient.com"))
    reversePending shouldBe 'isDefined
    reversePending.get.email should be ("user2@transient.com")
    val pending = Await.result(PendingUserRecord.getPendingUser(reversePending.get.token))
    pending shouldBe 'isDefined
    pending.get.email should be ("user2@transient.com")
  }

  "registering a transient user then confirming" should "create a registered user" in {
    val userId = UUID.fromString("b3cd8f16-502b-4947-9034-1ea5ad30500d")
    val transientId = UUID.fromString("ec2f78e8-d3a2-47c4-9efb-fb47c21821cf")
    val create = TransientUser.insertUser(TransientUser("user3@transient.com", userId, transientId))
    Await.result(create)
    val response = loginApp.get(s"/landing/$transientId?then=/register")
    response.status should be (FOUND)
    response.getHeader("location") should be ("/register")
    response.getHeader("set-cookie").indexOf("SessionId=") should be >= 0
    val sessionId = response
      .getHeader("set-cookie").split(";").map(_.trim().split("=")).find(a => a(0).equals("SessionId")).get(1)
    val registerResponse = userApp.post("/register", Map("email" -> "user3@transient.com",
      "password" -> "ch@ng3it!", "confirm" -> "ch@ng3it!"), Map("Cookie" -> s"SessionId=$sessionId"))
    registerResponse.status should be (OK)
    val reversePending = Await.result(ReversePendingUserRecord.getByEmail("user3@transient.com"))
    reversePending shouldBe 'isDefined
    reversePending.get.email should be ("user3@transient.com")
    val pending = Await.result(PendingUserRecord.getPendingUser(reversePending.get.token))
    pending shouldBe 'isDefined
    pending.get.email should be ("user3@transient.com")
    pending.get.user should be (userId)
    val verifyResponse = verifyApp.get(s"/verify/${pending.get.id}")
    verifyResponse.status should be (OK)

    val transient2 = Await.result(TransientUserRecord.forEmail("user3@tranient.com"))
    transient2 shouldBe 'isEmpty
    val transient3 = Await.result(ReverseTransientUserRecord.forUuid(userId))
    transient3 shouldBe 'isEmpty
    val pending2 = Await.result(PendingUserRecord.getPendingUser(pending.get.id))
    pending2 shouldBe 'isEmpty
    val pending3 = Await.result(ReversePendingUserRecord.getByEmail("user3@transient.com"))
    pending3 shouldBe 'isEmpty
    val user = Await.result(UserRecord.getByEmail("user3@transient.com"))
    user shouldBe 'isDefined
    user.get.email should be ("user3@transient.com")
    user.get.id should be (userId)
  }

  "registering with invalid email" should "return an error" in {
    val userId = UUID.fromString("0683e3d5-8d83-4efd-9b90-1d01478117c7")
    val transientId = UUID.fromString("345ff074-644a-499a-b259-40405ba93b8f")

    val create = TransientUser.insertUser(TransientUser("invalid@transient.com", userId, transientId))
    Await.result(create)
    val response = loginApp.get(s"/landing/$transientId?then=/register")
    response.status should be (FOUND)
    response.getHeader("location") should be ("/register")
    response.getHeader("set-cookie").indexOf("SessionId=") should be >= 0
    val sessionId = response
      .getHeader("set-cookie").split(";").map(_.trim().split("=")).find(a => a(0).equals("SessionId")).get(1)
    val registerResponse = userApp.post("/register", Map("email" -> "poorlyFormattedEmailAddress.com",
      "password" -> "ch@ng3it!", "confirm" -> "ch@ng3it!"), Map("Cookie" -> s"SessionId=$sessionId"))
    registerResponse.status should be (BAD_REQUEST)
  }

  "registering with mismatched passwords" should "return an error" in {
    val userId = UUID.fromString("97761752-3fce-4a53-96f5-6c03cbc101de")
    val transientId = UUID.fromString("1ac280cb-a12d-46c4-a233-4a7120cd15f7")

    val create = TransientUser.insertUser(TransientUser("invalid2@transient.com", userId, transientId))
    Await.result(create)
    val response = loginApp.get(s"/landing/$transientId?then=/register")
    response.status should be (FOUND)
    response.getHeader("location") should be ("/register")
    response.getHeader("set-cookie").indexOf("SessionId=") should be >= 0
    val sessionId = response
      .getHeader("set-cookie").split(";").map(_.trim().split("=")).find(a => a(0).equals("SessionId")).get(1)
    val registerResponse = userApp.post("/register", Map("email" -> "invalid2@transient.com",
      "password" -> "ch@ng3it!", "confirm" -> "ch@ngeit!"), Map("Cookie" -> s"SessionId=$sessionId"))
    registerResponse.status should be (BAD_REQUEST)
  }
}
