package org.showgregator.service.session

import com.twitter.util.Future
import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/24/15
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */
trait SessionStore {
  def get(id: UUID): Future[Option[Session]]
  def put(session: Session): Future[Boolean]
  def delete(id: UUID): Future[Boolean]
  def extend(id: UUID): Future[Boolean]
  def exists(id: UUID): Future[Boolean] = {
    get(id).map(_.isDefined)
  }
}
