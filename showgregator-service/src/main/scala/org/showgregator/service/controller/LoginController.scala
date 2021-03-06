package org.showgregator.service.controller

import com.websudos.phantom.Implicits.Session
import org.showgregator.core.crypto.PasswordHashing
import org.showgregator.core.geo.Location
import org.showgregator.core.util.UUIDs
import scala.concurrent.ExecutionContext
import org.showgregator.service.model._
import com.twitter.util.{Duration, Future}
import java.security.MessageDigest
import com.twitter.finatra.ResponseBuilder
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import org.showgregator.service.session.{Session => UserSession, SessionStore}
import java.util.UUID
import org.joda.time.DateTime
import org.showgregator.service.view.{StillPendingView, BadRequestView, ForbiddenView, ServerErrorView}
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
        for {
          location <- {
            request.params.get("lat").flatMap(lat => request.params.get("lon").map(lon => (lat, lon))) match {
              case Some((lat, lon)) => Location.findByGeolocationStrings(lat, lon).asFinagle
                // TODO, probably need to inspect load balancer header, if load balanced.
              case None => Location.findByAddress(request.remoteAddress).asFinagle
            }
          }
          user:Option[User] <- UserRecord.getByEmail(email)
          reversePendingUser <- if (user.isEmpty)
            ReversePendingUserRecord.getByEmail(email)
          else
            Future.value(None)
          pendingUser <- if (reversePendingUser.isDefined)
            PendingUserRecord.getPendingUser(reversePendingUser.get.token)
          else
            Future.value(None)
          hashedPassword <- (user, pendingUser) match {
            case (Some(u), None) => Future(PasswordHashing(password.toCharArray,
              u.hashedPassword.iterations, Some(u.hashedPassword.salt),
              u.hashedPassword.alg))
            case (None, Some(pu)) => Future(PasswordHashing(password.toCharArray,
              pu.hashedPassword.iterations, Some(pu.hashedPassword.salt),
              pu.hashedPassword.alg))
            case _ => Future(PasswordHashing(password.toCharArray))
          }
          session <- user match {
            case Some(u) => {
              val s = UserSession(UUID.randomUUID(), u.withCity(u.city.flatMap(_ => location)), DateTime.now(), DateTime.now().plusHours(12))
              sessionStore.put(s).map(if (_) Some(s) else None)
            }
            case None => Future.value(None)
          }

          hashesMatch <- if (user.isDefined)
            Future(MessageDigest.isEqual(hashedPassword.hash, user.get.hashedPassword.hash))
          else if (pendingUser.isDefined)
            Future(MessageDigest.isEqual(hashedPassword.hash, pendingUser.get.hashedPassword.hash))
          else
            Future(MessageDigest.isEqual(hashedPassword.hash, hashedPassword.hash)).map(_ => false)

          response <- if (user.isDefined && hashesMatch) {
            if (session.isDefined)
              redirect(nextPage(request.params.get("then")), "logged in", permanent = false).cookie(makeCookie(session.get.id)).toFuture
            else
              render.view(new ServerErrorView("logging in failed, try again later")).status(503).toFuture
          } else if (pendingUser.isDefined && hashesMatch) {
            render.view(new StillPendingView(pendingUser.get.email)).toFuture
          } else {
            render.static("/html/401.html").unauthorized.toFuture
          }
        } yield response
      }
      case _ => render.static("/html/401.html").unauthorized.toFuture
    }
  }

  get("/logout") { request =>
    request.cookies.get("SessionId") match {
      case Some(sid) => sessionStore.get(UUID.fromString(sid.value)).flatMap {
        case Some(userSession) => sessionStore.delete(userSession.id).flatMap(_ => {
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
        case Some(sid) => sessionStore.get(sid)
        case None => Future.value(None)
      }
      transientUser:Option[TransientUser] <- uuid match {
        case Some(uid) => ReverseTransientUserRecord.forUuid(uid)
        case None => Future.value(None)
      }
      response:ResponseBuilder <- (transientUser, userSession) match {
          // There is a valid session and a user, match the session to the user.
        case (Some(user), Some(sess)) => {
          log.debug("matching user %s with session %s", user, sess)
          if (user.email.equals(sess.user.email)) {
            sessionStore.extend(sess.id).flatMap(_ => redirect(nextPage(then)).toFuture)
          } else {
            render.view(new ForbiddenView()).status(403).toFuture
          }
        }

          // The user is valid, no session. Make a session and redirect.
        case (Some(user), None) => {
          log.debug("logging in %s", user)
          val newSession = UserSession(UUID.randomUUID(), user, DateTime.now(), DateTime.now())
          sessionStore.put(newSession).flatMap {
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
