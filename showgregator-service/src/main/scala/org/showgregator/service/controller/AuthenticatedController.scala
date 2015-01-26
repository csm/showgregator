package org.showgregator.service.controller

import com.twitter.finatra.{ResponseBuilder, Controller}
import org.showgregator.service.model._
import com.twitter.finagle.http.{Response, Request}
import org.showgregator.service.session.SessionStore
import java.util.UUID
import com.twitter.util.Future
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
class PhantomConnectedController(implicit val session: Session, val context: ExecutionContext) extends ControllerBase

class AuthenticatedController(implicit val sessionStore:SessionStore, override val session: Session, override val context: ExecutionContext) extends PhantomConnectedController {
  def user(request: Request):Future[Option[Either[User, TransientUser]]] = {
    request.cookies.get("SessionId") match {
      case Some(cookie) => {
        for {
          userSession <- sessionStore.get(UUID.fromString(cookie.value))
          user <- userSession match {
            case Some(s) if s.loggedIn => UserRecord.getByEmail(s.email).map(u => (u, None))
            case Some(s) if !s.loggedIn => TransientUserRecord.forEmail(s.email).map(u => (None, u))
            case None => ScalaFuture.successful((None, None))
          }
        } yield {
          user match {
            case (Some(u), None) => Some(Left(u))
            case (None, Some(u)) => Some(Right(u))
            case _ => None
          }
        }
      }.asFinagle

      case None => Future(None)
    }
  }

  def authenticated(request: Request): Future[Boolean] =
    user(request).map(_.isDefined)

  def !!!(request: Request)(service: (Request)=>Future[ResponseBuilder]): Future[ResponseBuilder] = {
    authenticated(request).flatMap({
      case true => service(request)
      case false => Future(redirect("/login", "Please log in.", permanent = false))
    })
  }
}
