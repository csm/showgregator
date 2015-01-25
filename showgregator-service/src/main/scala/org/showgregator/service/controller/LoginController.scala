package org.showgregator.service.controller

import com.websudos.phantom.Implicits.Session
import scala.concurrent.ExecutionContext

class LoginController(implicit override val session:Session, override val context: ExecutionContext) extends PhantomConnectedController {
  get("/login") { _ =>
    render.static("/html/login.html").toFuture
  }

  post("/login") { request =>
    render.plain("ok, logging you in").toFuture
  }
}
