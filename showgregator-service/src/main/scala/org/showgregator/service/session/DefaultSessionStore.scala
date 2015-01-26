package org.showgregator.service.session

import java.util.UUID
import scala.concurrent.Future
import org.joda.time.DateTime

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
class DefaultSessionStore extends SessionStore {
  case class SessionHolder(session: Session, expires: DateTime)

  private val sessions = new scala.collection.concurrent.TrieMap[UUID, SessionHolder]()

  def get(id: UUID): Future[Option[Session]] = {
    sessions.get(id) match {
      case Some(s) => if (s.expires.isBefore(DateTime.now())) {
        Future.successful(None)
      } else {
        Future.successful(Some(s.session))
      }
      case None => Future.successful(None)
    }
  }

  def put(session: Session): Future[Boolean] = {
    sessions += (session.id -> SessionHolder(session, DateTime.now().plusHours(1)))
    Future.successful(true)
  }

  def delete(id: UUID): Future[Boolean] = {
    Future.successful(sessions.remove(id).isDefined)
  }

  def extend(id: UUID): Future[Boolean] = {
    sessions.get(id) match {
      case Some(s) => put(s.session)
      case None => Future.successful(false)
    }
  }
}
