package org.showgregator.service.session

import java.util.UUID
import com.twitter.util.Future
import org.joda.time.DateTime
import com.twitter.finatra.Logging
import com.twitter.logging.Logger

class DefaultSessionStore extends SessionStore {
  val log = Logger("finatra")

  case class SessionHolder(session: Session, expires: DateTime)

  private val sessions = new scala.collection.concurrent.TrieMap[UUID, SessionHolder]()

  def get(id: UUID): Future[Option[Session]] = {
    log.debug("lookup session %s", id)
    sessions.get(id) match {
      case Some(s) => if (s.expires.isBefore(DateTime.now())) {
        sessions.remove(id)
        log.debug("session %s expired", id)
        Future.value(None)
      } else {
        log.debug("got session %s", s.session)
        Future.value(Some(s.session))
      }
      case None => {
        log.debug("no session for %s", id)
        Future.value(None)
      }
    }
  }

  def put(session: Session): Future[Boolean] = {
    log.debug("putting session %s", session)
    sessions += (session.id -> SessionHolder(session, DateTime.now().plusHours(1)))
    log.debug("sessions: %s", sessions)
    Future.value(true)
  }

  def delete(id: UUID): Future[Boolean] = {
    log.debug("delete session %s", id)
    Future.value(sessions.remove(id).isDefined)
  }

  def extend(id: UUID): Future[Boolean] = {
    log.debug("extending session %s", id)
    sessions.get(id) match {
      case Some(s) => put(s.session)
      case None => {
        log.debug("no session %s to extend", id)
        Future.value(false)
      }
    }
  }
}
