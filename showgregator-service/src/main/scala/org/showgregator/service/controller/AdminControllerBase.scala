package org.showgregator.service.controller

import java.security.Permission

import com.twitter.finatra.{Controller, ResponseBuilder, Request}
import com.twitter.util.{Return, Try, Future}
import org.apache.commons.codec.binary.Base64
import org.showgregator.service.admin.AdminAuthStore
import org.showgregator.service.view.ForbiddenView

class AdminControllerBase(implicit val adminStore: AdminAuthStore) extends Controller {
  def !!!(permission: Permission)(request: Request)(service: (Request)=>Future[ResponseBuilder]): Future[ResponseBuilder] = {
    log.info("permission: %s", permission)
    request.authorization match {
      case Some(s) if s.toLowerCase.startsWith("basic ") => {
        val auth = Try(new String(Base64.decodeBase64(s.substring(6)), "US-ASCII"))
          .flatMap(a => Try({
            val parts = a.split(":", 2)
            (parts(0), parts(1))
          }))
        auth match {
          case Return(a) => {
            log.info("auth: %s", a)
            adminStore.authenticate(a._1, a._2).flatMap({
              case Some(admin) => admin.hasPermission(permission).flatMap({
                case true => service(request)
                case false => render.view(new ForbiddenView("You don't have permission to do that.")).toFuture
              })

              case None => render.static("html/401.html").unauthorized.header("WWW-Authenticate", "Basic realm=\"SHOWGREGATOR admin\"").toFuture
            })
          }
        }
      }

      case _ => {
        render.static("html/401.html").unauthorized.header("WWW-Authenticate", "Basic realm=\"SHOWGREGATOR admin\"").toFuture
      }
    }
  }
}
