package org.showgregator.service.controller

import com.websudos.phantom.Implicits.Session
import scala.concurrent.ExecutionContext
import org.showgregator.service.model.UserRecord
import com.twitter.util.Future
import org.showgregator.core.{HashedPassword, PasswordHashing}
import java.security.MessageDigest
import com.twitter.finatra.ResponseBuilder
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper

class LoginController(implicit override val session:Session, override val context: ExecutionContext) extends PhantomConnectedController {
  get("/login") { _ =>
    render.static("/html/login.html").toFuture
  }

  post("/login") { request =>
    (request.params.get("email"), request.params.get("password")) match {
      case (Some(email), Some(password)) => {
        UserRecord.getByEmail(email).asFinagle.flatMap {
          case Some(user) => Future(PasswordHashing(password.toCharArray,
              user.hashedPassword.iterations, Some(user.hashedPassword.salt),
              user.hashedPassword.alg))
            .flatMap(hash => if (MessageDigest.isEqual(hash.hash, user.hashedPassword.hash)) {
              redirect(request.params.getOrElse("redirect", "/"), "logged in", permanent = false).toFuture
            } else {
              render.status(401).static("/html/401.html").toFuture
            })

          case None => Future(PasswordHashing("the powers that be".toCharArray))
            .flatMap(hash => {
              MessageDigest.isEqual(hash.hash, hash.hash)
              render.status(401).static("/html/401.html").toFuture
            })
        }
      }
      case _ => render.status(401).static("/401.html").toFuture
    }
  }
}
