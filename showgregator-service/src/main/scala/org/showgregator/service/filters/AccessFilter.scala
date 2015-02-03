package org.showgregator.service.filters

import java.util.UUID
import java.util.concurrent._

import com.datastax.driver.core.Session
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.logging.Logger
import com.twitter.util.{Await, FuturePool, Future}
import org.showgregator.service.model.{UserAccess, UserAccessRecord}
import org.showgregator.service.session.SessionStore

object AccessFilter {
  val log = Logger("finatra")

  val backgroundGroup = new ThreadGroup("AccessFilterThreads")

  val backgroundPool = FuturePool(new ThreadPoolExecutor(1, 4, 1, TimeUnit.MINUTES,
    new ArrayBlockingQueue[Runnable](32), new ThreadFactory {
      override def newThread(r: Runnable): Thread = {
        val thread = new Thread(backgroundGroup, r)
        thread.setDaemon(true)
        thread.setPriority(Thread.MIN_PRIORITY)
        thread
      }
    }, new RejectedExecutionHandler {
      override def rejectedExecution(r: Runnable, executor: ThreadPoolExecutor): Unit = {
        log.info("rejected run of user access")
      }
    }))
}

class AccessFilter(implicit val sessionStore: SessionStore, implicit val session: Session) extends SimpleFilter[Request, Response] {
  import AccessFilter._

  def logAccess(request: Request): Unit = {
    try {
      backgroundPool(request.cookies.get("SessionId") match {
        case Some(cookie) => {
          val sid = UUID.fromString(cookie.value)
          Await.result(sessionStore.get(sid).flatMap({
            case Some(s) => UserAccessRecord.insertAccess(UserAccess(s.user.userId, sid, request.path))
            case None => Future.value(null)
          }))
        }

        case None => Unit
      })
    } catch {
      case t: Throwable => log.debug(t, "exception posting user access")
    }
  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    logAccess(request)
    service(request)
  }
}
