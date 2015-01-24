package org.showgregator.service.finagle

import com.twitter.finagle.{Service, Filter}
import org.jboss.netty.handler.codec.http._
import com.twitter.util.Future

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/23/15
 * Time: 9:40 PM
 * To change this template use File | Settings | File Templates.
 */
class ExceptionsFilter extends Filter[HttpRequest, HttpResponse, HttpRequest, HttpResponse] {
  def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = try {
    service(request)
  } catch {
    case x:Exception => {
      val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR)
      Future(response)
    }
  }
}
