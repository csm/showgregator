package org.showgregator.service.controller

import com.websudos.phantom.Implicits.Session
import scala.concurrent.ExecutionContext
import org.showgregator.service.model.UserRecord
import com.twitter.util.{Duration, Future}
import org.showgregator.core.{HashedPassword, PasswordHashing}
import java.security.MessageDigest
import com.twitter.finatra.ResponseBuilder
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import org.showgregator.service.session.{Session => UserSession, SessionStore}
import java.util.UUID
import org.joda.time.DateTime
import org.showgregator.service.view.ServerErrorView
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import com.twitter.finagle.http.Cookie

class LoginController(implicit val sessionStore: SessionStore,
                      implicit override val session:Session,
                      override val context: ExecutionContext)
  extends PhantomConnectedController {
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
              val s = UserSession(UUID.randomUUID(), user.email, loggedIn = true, DateTime.now(), DateTime.now().plusHours(12))
              sessionStore.put(s).asFinagle.flatMap {
                case true => redirect(request.params.getOrElse("then", "/home"), "logged in", permanent = false).cookie("SessionId", s.id.toString).toFuture
                case false => render.view(new ServerErrorView("logging in failed, try again later")).status(503).toFuture
              }
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

  get("/logout") { request =>
    request.cookies.get("SessionId") match {
      case Some(sid) => sessionStore.get(UUID.fromString(sid.value)).asFinagle.flatMap {
        case Some(userSession) => sessionStore.delete(userSession.id).asFinagle.flatMap(_ => {
          val cookie = new Cookie("SessionId", "")
          cookie.isDiscard = true
          redirect("/", "logged out", permanent = false).cookie(cookie).toFuture
        })
        case None => redirect("/", "not logged in", permanent = false).toFuture
      }

      case None => redirect("/", "not logged in", permanent = false).toFuture
    }
  }
}
