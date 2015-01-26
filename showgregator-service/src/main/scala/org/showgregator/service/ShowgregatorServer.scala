package org.showgregator.service

import com.twitter.finatra.FinatraServer
import org.showgregator.service.controller._
import com.datastax.driver.core.Cluster
import org.showgregator.service.session.DefaultSessionStore

object ShowgregatorServer extends FinatraServer {
  import scala.concurrent.ExecutionContext.Implicits.global

  val cluster = Cluster.builder()
    .addContactPoint("127.0.0.1")
    .build()
  implicit val session = cluster.connect("showgregator")
  implicit val sessionStore = new DefaultSessionStore

  log.info("running on java version: %s", System.getProperty("java.version"))

  register(new RootController)
  register(new LoginController)
  register(new RegisterController)
  register(new VerifyEmailController)
  register(new UserController)
}
