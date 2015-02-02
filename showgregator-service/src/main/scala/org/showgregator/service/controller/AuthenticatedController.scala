package org.showgregator.service.controller

import com.twitter.finatra.{Request, ResponseBuilder}
import org.showgregator.service.model._
import org.showgregator.service.session.{Session => UserSession, SessionStore}
import java.util.UUID
import com.twitter.util.{Throw, Return, Try, Future}
import org.showgregator.service.model.User
import scala.Some
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import scala.concurrent.{Future => ScalaFuture, ExecutionContext}
import com.websudos.phantom.Implicits.Session

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/24/15
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
class PhantomConnectedController(implicit val session: Session,
                                 implicit val context: ExecutionContext) extends ControllerBase

class AuthenticatedController(implicit val sessionStore:SessionStore,
                              override val session: Session,
                              override val context: ExecutionContext) extends PhantomConnectedController {
  def user(request: Request):Future[Option[BaseUser]] = {
    request.cookies.get("SessionId") match {
      case Some(cookie) => {
        log.debug("looking up session %s", cookie.value)
        for {
          sessionId <- Future.value(Try(UUID.fromString(cookie.value)))
          userSession:Option[UserSession] <- sessionId match {
            case Return(sid) => sessionStore.get(sid).asFinagle
            case Throw(_) => Future.value(None)
          }
          user <- userSession match {
            case Some(s) => {
              log.debug("found session %s", s)
              Future.value(Some(s.user))
            }

            case None => {
              log.debug("no session found")
              Future.value(None)
            }
          }
        } yield user
      }

      case None => {
        log.debug("no session ID")
        Future.value(None)
      }
    }
  }

  def authenticated(request: Request): Future[Boolean] =
    user(request).map(_.isDefined)

  /**
   * Mark your resource as requiring authentication.
   *
   * E.g. get("/foo") { !!! { request => ... } }
   *
   * @param service
   * @param request
   * @return
   */
  def !!!(service: (Request, BaseUser)=>Future[ResponseBuilder])(request: Request): Future[ResponseBuilder] = {
    user(request).flatMap({
      case Some(user) => service(request, user)
      case None => redirect(s"/login#${request.uri}", "Please log in.", permanent = false).toFuture
    })
  }
}
