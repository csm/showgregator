package org.showgregator.service.controller

import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import org.showgregator.service.model.{UserRecord, RegisterToken, RegisterTokenRecord}
import java.util.UUID
import org.showgregator.service.view.{ForbiddenView, RegisterView}
import com.twitter.finatra.ResponseBuilder
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import com.twitter.util.{Future, Return, Try}
import com.twitter.finagle.http.Request
import org.showgregator.core.shaded.uk.gov.hmrc.emailaddress.EmailAddress

class RegisterController(implicit override val session:Session, override val context: ExecutionContext) extends PhantomConnectedController {

  get("/register/:token") { request =>
    val token = Try(UUID.fromString(request.routeParams.get("token").get)).rescue({
      case _ => Return(new UUID(0, 0))
    }).get()
    for {
      token:Option[RegisterToken] <- RegisterTokenRecord.findToken(token).asFinagle
      response:ResponseBuilder <- token match {
        case Some(t) => render.view(new RegisterView(t.token.toString, t.email.getOrElse(""))).toFuture
        case None => render.view(new ForbiddenView("Invalid token.")).status(403).toFuture
      }
    } yield response
  }

  def isValidRegistration(request:Request):Future[Boolean] = {
    if (!request.params.get("password").isDefined
        || !request.params.get("password").equals(request.params.get("confirm"))) {
      return Future(false)
    }
    if (!request.params.get("email").isDefined
        || !EmailAddress.isValid(request.params.get("email").get)) {
      return Future(false)
    }
    UserRecord.getByEmail(request.params.get("email").get).map(u => u.isEmpty).asFinagle
  }

  post("/register/:token") { request =>
    val token = Try(UUID.fromString(request.routeParams.get("token").get)).rescue({
      case _ => Return(new UUID(0, 0))
    }).get()
    for {
      valid:Boolean <- isValidRegistration(request)
      token:Option[RegisterToken] <- if (valid) {
        RegisterTokenRecord.findToken(token).asFinagle
      } else {
        Future(None)
      }
      response:ResponseBuilder <- token match {
        case Some(t) => {
          Future.exception(new UnsupportedOperationException("not yet implemented"))
        }
        case None => render.view(new ForbiddenView("Registration invalid.")).status(403).toFuture
      }
    } yield response
  }
}
