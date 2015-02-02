package org.showgregator.service

import java.util.UUID

import com.twitter.finatra.test.MockApp
import com.twitter.util.Await
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.Matchers
import org.showgregator.service.controller.LoginController
import org.showgregator.service.model.User


/**
 * Created by cmarshall on 2/1/15.
 */
class LoginSpec extends AbstractServiceSpec with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global
  val loginApp = MockApp(new LoginController)

  "get login page" should "return success" in {
    val response = loginApp.get("/login")
    response.status should be (OK)
  }

  "log in with wrong email" should "return an error" in {
    val response = loginApp.post("/login", Map("email" -> "not.there@login.com",
      "password" -> "changeit"))
    response.status should be (UNAUTHORIZED)
  }

  "log in with wrong password" should "return an error" in {
    Await.result(User.createUser(UUID.fromString("bc9b85d8-5ab5-4283-874e-811b8e6c4997"),
      "hackme@login.com", None, "secret".toCharArray))
    val response = loginApp.post("/login", Map("email" -> "hackme@login.com", "password" -> "hackyou"))
    response.status should be (UNAUTHORIZED)
  }

  "log in with correct password" should "succeed" in {
    Await.result(User.createUser(UUID.fromString("8be9d5f9-ba7f-4f2c-98c3-1e320183bd33"),
      "user@login.com", None, "changeit".toCharArray))
    val response = loginApp.post("/login", Map("email" -> "user@login.com", "password" -> "changeit"))
    response.status should be (FOUND)
    response.getHeader("set-cookie").indexOf("SessionId=") should be >= 0
  }
}
