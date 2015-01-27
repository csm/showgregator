package org.showgregator.service.controller

import com.websudos.phantom.Implicits.Session
import scala.concurrent.ExecutionContext
import org.showgregator.service.model._
import com.twitter.util.{Duration, Future}
import org.showgregator.core.{UUIDs, HashedPassword, PasswordHashing}
import java.security.MessageDigest
import com.twitter.finatra.ResponseBuilder
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import org.showgregator.service.session.{Session => UserSession, SessionStore}
import java.util.UUID
import org.joda.time.DateTime
import org.showgregator.service.view.{BadRequestView, ForbiddenView, ServerErrorView}
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import com.twitter.finagle.http.Cookie
import com.websudos.phantom.Implicits.Session
import org.showgregator.service.session.{Session => UserSession}
import scala.Some

class LoginController(implicit val sessionStore: SessionStore,
                      implicit override val session:Session,
                      override val context: ExecutionContext)
  extends PhantomConnectedController {
  get("/login") { _ =>
    render.static("/html/login.html").toFuture
  }

  def nextPage(next:Option[String], orElse:String = "/home"):String = {
    // TODO validate URL
    next match {
      case Some(s) if s.trim.length > 0 => s
      case _ => orElse
    }
  }

  def makeCookie(id: UUID):Cookie = {
    val cookie = new Cookie("SessionId", id.toString)
    cookie.path = "/"
    cookie
  }

  post("/login") { request =>
    (request.params.get("email"), request.params.get("password")) match {
      case (Some(email), Some(password)) => {
        log.info("next: %s", request.params.getOrElse("then", "<NONE>"))
        UserRecord.getByEmail(email).asFinagle.flatMap {
          case Some(user) => Future(PasswordHashing(password.toCharArray,
              user.hashedPassword.iterations, Some(user.hashedPassword.salt),
              user.hashedPassword.alg))
            .flatMap(hash => if (MessageDigest.isEqual(hash.hash, user.hashedPassword.hash)) {
              val s = UserSession(UUID.randomUUID(), user, DateTime.now(), DateTime.now().plusHours(12))
              sessionStore.put(s).asFinagle.flatMap {
                case true => redirect(nextPage(request.params.get("then")), "logged in", permanent = false).cookie(makeCookie(s.id)).toFuture
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
          cookie.path = "/"
          cookie.isDiscard = true
          redirect("/", "logged out", permanent = false).cookie(cookie).toFuture
        })
        case None => redirect("/", "not logged in", permanent = false).toFuture
      }

      case None => redirect("/", "not logged in", permanent = false).toFuture
    }
  }

  get("/landing/:id") { request =>
    val sessionId = request.cookies.get("SessionId").flatMap(c => UUIDs.parseUUID(Some(c.value)))
    val uuid = UUIDs.parseUUID(request.routeParams.get("id"))
    val then = request.params.get("then")
    log.debug("sessionId: %s uuid: %S then: %s", sessionId, uuid, then)
    for {
      userSession:Option[UserSession] <- sessionId match {
        case Some(sid) => sessionStore.get(sid).asFinagle
        case None => Future.value(None)
      }
      pendingUser:Option[TransientUser] <- uuid match {
        case Some(uid) => ReverseTransientUserRecord.forUuid(uid).asFinagle
        case None => Future.value(None)
      }
      response:ResponseBuilder <- (pendingUser, userSession) match {
          // There is a valid session and a user, match the session to the user.
        case (Some(user), Some(sess)) => {
          log.debug("matching user %s with session %s", user, sess)
          if (user.email.equals(sess.user.email)) {
            sessionStore.extend(sess.id).asFinagle.flatMap(_ => redirect(nextPage(then)).toFuture)
          } else {
            render.view(new ForbiddenView()).status(403).toFuture
          }
        }

          // The user is valid, no session. Make a session and redirect.
        case (Some(user), None) => {
          log.debug("logging in %s", user)
          val newSession = UserSession(UUID.randomUUID(), user, DateTime.now(), DateTime.now())
          sessionStore.put(newSession).asFinagle.flatMap {
            case true => redirect(nextPage(request.params.get("then"))).cookie(makeCookie(newSession.id)).toFuture
            case false => render.view(new ServerErrorView("")).status(503).toFuture
          }
        }

          // Not a valid user, but a valid session (so you're logged in as someone else),
          // just redirect to the next place.
        case (None, Some(sess)) => {
          log.debug("already logged in as %s", sess)
          redirect(nextPage(request.params.get("then"))).toFuture
        }
        case (None, None) => {
          log.debug("no user and no session")
          render.view(new BadRequestView()).status(400).toFuture
        }
      }
    } yield response
  }
}
