package org.showgregator.service

import java.util.UUID

import com.twitter.finatra.test.MockApp
import com.twitter.util.Await
import org.scalatest.Matchers
import org.showgregator.service.controller.RegisterController
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.showgregator.service.model._

/**
 * Created by cmarshall on 2/1/15.
 */
class PendingUserSpec extends AbstractServiceSpec with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  val registerApp = MockApp(new RegisterController)

  "accessing invalid token uuid" should "return bad request" in {
    val response = registerApp.get("/register/not-a-real-uuid")
    response.status should be (BAD_REQUEST)
  }

  "accessing invalid token" should "return forbidden" in {
    val response = registerApp.get("/register/6fa168c3-0816-4acd-8b5c-6d31bea16f45")
    response.status should be (FORBIDDEN)
  }

  "posting to invalid token uuid" should "return bad request" in {
    val response = registerApp.post("/register/not-a-real-uuid", Map("email" -> "user@pending.com",
      "password" -> "changeit", "confirm" -> "changeit"))
    response.status should be (BAD_REQUEST)
  }

  "posting to invalid token" should "return forbidden" in {
    val response = registerApp.post("/register/08abafbf-5c1e-4b61-bad4-10bbd63a5009", Map("email" -> "user@pending.com",
      "password" -> "changeit", "confirm" -> "changeit"))
    response.status should be (FORBIDDEN)
  }

  "posting email to invalid token" should "return bad request" in {
    val response = registerApp.post("/register/2b5bf7bb-0908-4d98-84d5-f490296d0e42", Map("email" -> "thatInvalidEmailGuy.com",
      "password" -> "changeit", "confirm" -> "changeit"))
    response.status should be (BAD_REQUEST)
  }

  "posting bad password to invalid token" should "return bad request" in {
    val response = registerApp.post("/register/2b5bf7bb-0908-4d98-84d5-f490296d0e42", Map("email" -> "user2@pending.com",
      "password" -> "changeit", "confirm" -> "changeeit"))
    response.status should be (BAD_REQUEST)
  }

  "posting bad email to real token" should "return bad request" in {
    val id = UUID.fromString("cb7b20a5-a12f-4434-9d44-ce986eed5649")
    Await.result(RegisterTokenRecord.insertToken(RegisterToken(id, None)))
    val response = registerApp.post(s"/register/$id", Map("email" -> "thatInvalidEmailGuy.com",
      "password" -> "changeit", "confirm" -> "changeit"))
    response.status should be (BAD_REQUEST)
    val token = Await.result(RegisterTokenRecord.findToken(id))
    token shouldBe 'isDefined
  }

  "posting bad password to real token" should "return bad request" in {
    val id = UUID.fromString("01ca8844-e874-4503-82ab-731426d6308c")
    Await.result(RegisterTokenRecord.insertToken(RegisterToken(id, None)))
    val response = registerApp.post(s"/register/$id", Map("email" -> "user3@pending.com",
      "password" -> "changeit", "confirm" -> "changeeit"))
    response.status should be (BAD_REQUEST)
    val token = Await.result(RegisterTokenRecord.findToken(id))
    token shouldBe 'isDefined
  }

  "a good registration" should "return success and create a pending user" in {
    val id = UUID.fromString("199d4af5-27e6-4ec0-a18b-0414e8171b0e")
    Await.result(RegisterTokenRecord.insertToken(RegisterToken(id, None)))
    val response = registerApp.post(s"/register/$id", Map("email" -> "user4@email.com",
      "password" -> "changeit", "confirm" -> "changeit"))
    response.status should be (OK)
    val revPending = Await.result(ReversePendingUserRecord.getByEmail("user4@email.com"))
    revPending shouldBe 'isDefined
    val pending = Await.result(PendingUserRecord.getPendingUser(revPending.get.token))
    pending shouldBe 'isDefined
    val token = Await.result(RegisterTokenRecord.findToken(id))
    token shouldBe 'isEmpty
  }
}
