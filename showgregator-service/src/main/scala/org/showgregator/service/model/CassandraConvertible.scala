package org.showgregator.service.model

import com.datastax.driver.core.{Session, BoundStatement, PreparedStatement}

/**
 * Created by cmarshall on 1/18/15.
 */
trait CassandraConvertible {
  def insertStatement(session: Session):BoundStatement
  def deleteStatement(session: Session):BoundStatement
}
