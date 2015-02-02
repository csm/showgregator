package org.showgregator.service.controller

import com.twitter.finatra.Controller
import java.io.{PrintWriter, StringWriter}
import com.twitter.util.Future

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/24/15
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
class RootController extends ControllerBase {
  get("/") { request =>
    render.static("/html/main.html")
      .contentType("text/html")
      .toFuture
  }

  get("/ping") { request =>
    render.plain("OK").ok.toFuture
  }

  get("/css/:stylesheet") { request =>
    render
      .static(s"/css/${request.routeParams.get("stylesheet")}")
      .contentType("text/css")
      .toFuture
  }

  get("/fail") { request =>
    try {
      throw new Exception("guaranteed fail")
    } catch {
      case t:Throwable => Future.exception(t)
    }
  }
}
