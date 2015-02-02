package org.showgregator.service.controller

import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import org.showgregator.service.model._
import java.util.UUID
import org.showgregator.service.view._
import com.twitter.finatra.ResponseBuilder
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import com.twitter.util.{Future, Return, Try}
import com.twitter.finagle.http.Request
import org.showgregator.core.shaded.uk.gov.hmrc.emailaddress.EmailAddress
import org.showgregator.core.PasswordHashing
import org.showgregator.service.model.PendingUser
import scala.Some
import org.showgregator.service.model.RegisterToken

object RegisterController {
  case class RegistrationCheckResult(valid: Boolean, message: String = "")

  def isValidRegistration(request:Request)(implicit session: Session):Future[RegistrationCheckResult] = {
    if (!request.params.get("password").isDefined
      || !request.params.get("password").equals(request.params.get("confirm"))) {
      return Future.value(RegistrationCheckResult(valid = false, "password arguments not valid"))
    }
    if (!(request.params.get("email").isDefined
          && EmailAddress.isValid(request.params.get("email").get))) {
      return Future.value(RegistrationCheckResult(valid = false, "email address is not valid"))
    }
    UserRecord.getByEmail(request.params.get("email").get).map(u => if(u.isEmpty)
      RegistrationCheckResult(valid = true)
    else
      RegistrationCheckResult(valid = false, s"Address ${request.params.get("email").get} already registered"))
  }
}

class RegisterController(implicit override val session:Session, override val context: ExecutionContext) extends PhantomConnectedController {
  import RegisterController._

  get("/register/:token") { request =>
    val tokenId = request.routeParams.get("token").map(UUID.fromString).get
    for {
      token:Option[RegisterToken] <- RegisterTokenRecord.findToken(tokenId)
      response:ResponseBuilder <- token match {
        case Some(t) => render.view(new RegisterView(Some(t.token.toString), t.email.getOrElse(""))).toFuture
        case None => render.view(new ForbiddenView("Invalid token.")).forbidden.toFuture
      }
    } yield response
  }

  post("/register/:token") { request =>
    for {
      tokenId: UUID <- Future(UUID.fromString(request.routeParams.get("token").get))
      check <- isValidRegistration(request)
      token <- check match {
        case c if c.valid => RegisterTokenRecord.findToken(tokenId)
        case c if !c.valid => Future.value(None)
      }
      taken <- (check, token) match {
        case (c, Some(t)) if c.valid => RegisterTokenRecord.takeToken(t,
          request.params.get("email").get,
          request.params.get("handle").flatMap(h => if (h.trim.isEmpty) None else Some(h)),
          request.params.get("password").get.toCharArray).flatMap({
          case Some(u) => render.view(new SuccessfulRegisterView(u.email)).toFuture
          case None => {
            log.error("failed to take register token %s", t)
            render.view(new ServerErrorView("Internal error. Please try again later.")).internalServerError.toFuture
          }
        })
        case (c, None) if c.valid => render.view(new ForbiddenView("Invalid token.")).forbidden.toFuture
        case (c, _) => render.view(new BadRequestView(c.message)).badRequest.toFuture
      }
    } yield taken
  }
}
