package org.showgregator.service

import java.util.UUID

import com.datastax.driver.core.Cluster
import com.twitter.logging.{Level, ConsoleHandler, Logger}
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import org.showgregator.service.session.DefaultSessionStore
import org.showgregator.service.tools.InitTables

/**
 * Created by cmarshall on 2/1/15.
 */
abstract class AbstractServiceSpec extends FlatSpec with BeforeAndAfterAll {
  val log = Logger("finatra")
  log.addHandler(new ConsoleHandler(level = Some(Level.ALL)))
  log.setLevel(Level.ALL)
  val keyspace = "showgregator_test_" + UUID.randomUUID().toString.split("-").head
  InitTables.run(keyspace)
  println(s"keyspace $keyspace created")
  val cassandra = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session = cassandra.connect(keyspace)
  implicit val sessionStore = new DefaultSessionStore

  override protected def afterAll(): Unit = {
    session.execute(s"DROP KEYSPACE $keyspace;")
    println(s"keyspace $keyspace dropped")
    super.afterAll()
  }
}
