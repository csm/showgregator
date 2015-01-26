package org.showgregator.service.controller

import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import org.showgregator.service.model._
import java.util.UUID
import org.showgregator.service.view.{SuccessfulRegisterView, BadRequestView, ForbiddenView, RegisterView}
import com.twitter.finatra.ResponseBuilder
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import com.twitter.util.{Future, Return, Try}
import com.twitter.finagle.http.Request
import org.showgregator.core.shaded.uk.gov.hmrc.emailaddress.EmailAddress
import org.showgregator.core.PasswordHashing
import org.showgregator.service.model.PendingUser
import scala.Some
import org.showgregator.service.model.RegisterToken

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
      throw new IllegalArgumentException("password arguments not valid")
    }
    if (!request.params.get("email").isDefined
        || !EmailAddress.isValid(request.params.get("email").get)) {
      throw new IllegalArgumentException("email address is not valid")
    }
    UserRecord.getByEmail(request.params.get("email").get).map(u => u.isEmpty).asFinagle
  }

  post("/register/:token") { request =>
    for {
      tokenId:UUID <- Future(UUID.fromString(request.routeParams.get("token").get))
      valid:Boolean <- isValidRegistration(request)
      token:Option[RegisterToken] <- if (valid) {
        RegisterTokenRecord.findToken(tokenId).asFinagle
      } else {
        Future(None)
      }
      response:ResponseBuilder <- {
        token match {
          case Some(t) => {
            for {
              pendingUser <- Future(PendingUser(UUID.randomUUID(),
                request.params.get("email").get, request.params.get("handle"),
                PasswordHashing(request.params.get("password").get.toCharArray)))
              insertUser <- PendingUserRecord.insertUser(pendingUser).asFinagle
              reverseUser <- ReversePendingUserRecord.insertUser(pendingUser).asFinagle
              deleteToken <- RegisterTokenRecord.delete.where(_.id eqs t.token).future().asFinagle
              response <- render.view(new SuccessfulRegisterView(pendingUser.email)).toFuture
            } yield response
          }
          case None => render.view(new ForbiddenView("Registration invalid.")).status(403).toFuture
        }
      }
    } yield response
  }
}