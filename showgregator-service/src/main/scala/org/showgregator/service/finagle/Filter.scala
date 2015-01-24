package org.showgregator.service.finagle

import com.twitter.finagle.{Filter => FinagleFilter, Service}
import scala.concurrent.Future
import com.twitter.util



object Filter {
  import Filter.AsFinagleFilter
  implicit class AsFinagleFilter[-ReqIn, +RepOut, +ReqOut, -RepIn](filter: Filter[ReqIn, RepOut, ReqOut, RepIn]) {
    def asFinagle:FinagleFilter[ReqIn, RepOut, ReqOut, RepIn] = new FinagleFilter[ReqIn, RepOut, ReqOut, RepIn] {
      def apply(request: ReqIn, service: Service[ReqOut, RepIn]): util.Future[RepOut] = {
        filter.apply(request).as
      }
    }
  }
}

abstract class Filter[-ReqIn, +RepOut, +ReqOut, -RepIn] {
  def apply(request: ReqIn, service: Service[ReqOut, RepIn]): Future[RepOut]
}
