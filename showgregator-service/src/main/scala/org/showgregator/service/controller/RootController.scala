package org.showgregator.service.controller

import com.twitter.finatra.Controller

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/24/15
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
class RootController extends Controller {
  get("/") { request =>
    render.static("/html/placeholder.html")
      .contentType("text/html")
      .toFuture
  }

  get("/css/:stylesheet") { request =>
    render
      .static(s"/css/${request.routeParams.get("stylesheet")}")
      .contentType("text/css")
      .toFuture
  }
}
