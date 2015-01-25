package org.showgregator.service.finagle

import scala.concurrent.{Future => ScalaFuture, ExecutionContext}
import com.twitter.util.{Promise, Future}
import scala.util.{Failure, Success}

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/23/15
 * Time: 9:48 PM
 * To change this template use File | Settings | File Templates.
 */
object FinagleFutures {
  implicit class ScalaFutureWrapper[A](future: ScalaFuture[A])(implicit context: ExecutionContext) {
    def asFinagle: Future[A] = {
      val promise = Promise[A]()
      future.andThen {
        case Success(a) => promise.setValue(a)
        case Failure(t) => promise.setException(t)
      }
      promise
    }
  }
}
