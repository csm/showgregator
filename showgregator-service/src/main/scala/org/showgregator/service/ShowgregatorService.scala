package org.showgregator.service

import com.twitter.finagle.Http
import com.twitter.finagle.Service
import com.twitter.util.{Await, Future}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http._

object ShowgregatorService extends App {
  val service = new Service[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest): Future[HttpResponse] = {
      val response: DefaultHttpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.setContent(ChannelBuffers.wrappedBuffer("<html>\n\t<head>\n\t\t<title>SHOWGREGATOR<br><br>COMING SOON</title>\n\t\t<style type=\"text/css\">\n\t\tdiv.main {\n\t\t\tfont-family: \"Helvetica Neue\", Helvetica, sans;\n\t\t\tfont-weight: bold;\n\t\t\tfont-size: xx-large;\n\t\t\ttext-align: center;\n\t\t\tposition: relative;\n\t\t\ttop: 50%;\n\t\t\ttransform: translateY(-50%);\n\t\t}\n\t\t</style>\n\t</head>\n\t<body>\n\t\t<div class=\"main\">SHOWGREGATOR.</div>\n\t</body>\n</html>".getBytes("UTf-8")))
      Future.value(response)
    }
  }

  val server = Http.serve(":8080", service)
  Await.ready(server)
}
