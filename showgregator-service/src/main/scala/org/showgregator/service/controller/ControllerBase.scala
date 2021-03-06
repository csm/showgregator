package org.showgregator.service.controller

import com.twitter.finatra.Controller
import org.showgregator.service.view.{BadRequestView, NotFoundView, ServerErrorView}
import java.io.{PrintWriter, StringWriter}


class ControllerBase extends Controller {
  notFound { request =>
    render.view(new NotFoundView(request.uri)).status(404).toFuture
  }

  error { request =>
    request.error match {
      case Some(iae:IllegalArgumentException) => {
        log.debug(iae, "caught exception")
        render.view(new BadRequestView()).status(400).toFuture
      }

      case Some(t:Throwable) => {
        // TODO don't print the stack in non dev mode
        log.warning(t, "caught exception")
        val sWriter = new StringWriter()
        t.printStackTrace(new PrintWriter(sWriter))
        render.view(new ServerErrorView("Ah shit.<br><br>Something failed on the server.", sWriter.toString)).status(500).toFuture
      }
      case None => render.view(new ServerErrorView("Ah shit.<br><br>Something failed on the server.")).status(500).toFuture
    }
  }
}
