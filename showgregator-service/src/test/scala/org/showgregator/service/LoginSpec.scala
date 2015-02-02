package org.showgregator.service

import com.twitter.finatra.test.MockApp
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.scalatest.Matchers
import org.showgregator.service.controller.LoginController


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
}
