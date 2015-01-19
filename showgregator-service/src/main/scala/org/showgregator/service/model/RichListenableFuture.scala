package org.showgregator.service.model

import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import scala.concurrent.{Promise, Future}
import scala.util.Success

/**
 * Created by cmarshall on 1/18/15.
 */
object RichListenableFuture {
  implicit class WrappedFuture[T](lf: ListenableFuture[T]) {
    def asScala:Future[T] = {
      val promise = Promise[T]()
      Futures.addCallback(lf, new FutureCallback[T] {
        override def onFailure(t: Throwable): Unit = promise.failure(t)
        override def onSuccess(result: T): Unit = promise.complete(Success(result))
      })
      promise.future
    }
  }
}
