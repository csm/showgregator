package org.showgregator.service.controller

import org.showgregator.service.session.SessionStore
import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import org.showgregator.service.view.UserHomeView

class UserController(implicit override val sessionStore:SessionStore, override val session: Session, override val context: ExecutionContext) extends AuthenticatedController {
  get("/home") {
    !!! { request =>
      user(request).flatMap {
        case Some(u) => render.view(new UserHomeView(if (u.isLeft) u.left.get else u.right.get)).toFuture
        case None => redirect("/login", "please log in", permanent = false).toFuture
      }
    }
  }
}
