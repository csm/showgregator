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
  def isValidRegistration(request:Request)(implicit session: Session):Future[Boolean] = {
    if (!request.params.get("password").isDefined
      || !request.params.get("password").equals(request.params.get("confirm"))) {
      throw new IllegalArgumentException("password arguments not valid")
    }
    if (!request.params.get("email").isDefined
      || !EmailAddress.isValid(request.params.get("email").get)) {
      throw new IllegalArgumentException("email address is not valid")
    }
    UserRecord.getByEmail(request.params.get("email").get).map(u => u.isEmpty)
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
        case None => render.view(new ForbiddenView("Invalid token.")).status(403).toFuture
      }
    } yield response
  }

  post("/register/:token") { request =>
    for {
      tokenId: UUID <- Future(UUID.fromString(request.routeParams.get("token").get))
      valid: Boolean <- isValidRegistration(request)
      token <- valid match {
        case true => RegisterTokenRecord.findToken(tokenId)
        case false => Future.value(None)
      }
      taken <- (valid, token) match {
        case (true, Some(t)) => RegisterTokenRecord.takeToken(t,
          request.params.get("email").get,
          request.params.get("handle").flatMap(h => if (h.trim.isEmpty) None else Some(h)),
          request.params.get("password").get.toCharArray).flatMap({
          case Some(u) => render.view(new SuccessfulRegisterView(u.email)).toFuture
          case None => {
            log.error("failed to take register token %s", t)
            render.view(new ServerErrorView("Internal error. Please try again later.")).toFuture
          }
        })
        case (true, None) => render.view(new ForbiddenView("Invalid token.")).toFuture
        case _ => render.view(new BadRequestView("Invalid arguments. Please try again.")).toFuture
      }
    } yield taken
  }
}
