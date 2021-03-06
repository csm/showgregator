package org.showgregator.service.controller

import org.showgregator.service.model.{PendingUser, PendingUserRecord, TransientUser, User}
import org.showgregator.service.session.SessionStore
import com.websudos.phantom.Implicits._
import scala.concurrent.ExecutionContext
import org.showgregator.service.view._

class UserController(implicit override val sessionStore:SessionStore, override val session: Session, override val context: ExecutionContext) extends AuthenticatedController {
  import RegisterController._

  get("/home") {
    !!! {
      (request, user) => {
        render.view(new UserHomeView(user)).toFuture
      }
    }
  }

  get("/register") {
    !!! {
      (request, user) => {
        user match {
          case u:User => render.view(new BadRequestView("You are already registered.")).status(400).toFuture
          case tu:TransientUser =>
            render.view(new RegisterView(None, tu.email)).toFuture
        }
      }
    }
  }

  post("/register") {
    !!! {
      (request, user) => user match {
        case u:User => render.view(new BadRequestView("You are already registered.")).status(400).toFuture
        case tu:TransientUser => {
          val email = request.params.get("email")
          val password = request.params.get("password")
          log.debug("register with email: %s", email)
          for {
            check <- isValidRegistration(request)
            response <- {
              log.debug("check result: %s", check)
              check.valid match {
                case true =>
                  PendingUser.createUser(request.params.get("email").get,
                    request.params.get("handle"), request.params.get("password").get.toCharArray,
                    Some(tu.userId), Some(tu.email), Some(tu.id)).flatMap({
                    case Some(pu) => render.view(new SuccessfulRegisterView(pu.email)).toFuture
                    case None => {
                      log.error("registering %s failed!", tu)
                      render.view(new ServerErrorView("failed to register!")).internalServerError.toFuture
                    }
                  })
                case false => render.view(new BadRequestView(check.message)).badRequest.toFuture
              }
            }
          } yield response
        }
      }
    }
  }
}
