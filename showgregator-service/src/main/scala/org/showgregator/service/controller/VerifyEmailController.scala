package org.showgregator.service.controller

import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import java.util.UUID
import com.twitter.util.Future
import org.showgregator.service.model.{PendingUserRecord, PendingUser}
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import org.showgregator.service.view.{ServerErrorView, SuccessfulValidationView, ForbiddenView}

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 7:32 PM
 * To change this template use File | Settings | File Templates.
 */
class VerifyEmailController(implicit override val session: Session, override val context: ExecutionContext) extends PhantomConnectedController {
  get("/verify/:token") { request =>
    for {
      tokenId <- Future(UUID.fromString(request.routeParams.get("token").get))
      token <- PendingUserRecord.getPendingUser(tokenId)
      response <- token match {
        case Some(t) => PendingUserRecord.verifyUser(t.id).flatMap({
          case true => render.view(new SuccessfulValidationView(t.email)).toFuture
          case false => {
            log.error("failed to validate email! token: %s", t)
            render.view(new ServerErrorView("something failed internally, sorry")).status(500).toFuture
          }
        })
        case None => render.view(new ForbiddenView("Invalid token.")).status(403).toFuture
      }
    } yield response
  }
}
