package org.showgregator.service.model

import com.datastax.driver.core.Session

import scala.concurrent.Future

trait CassandraResolvable[T] {
  def resolve(session: Session):Future[T]
}
