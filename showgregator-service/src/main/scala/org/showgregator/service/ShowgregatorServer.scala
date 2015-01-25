package org.showgregator.service

import com.twitter.finatra.FinatraServer
import org.showgregator.service.controller.{LoginController, RootController}
import com.datastax.driver.core.Cluster

object ShowgregatorServer extends FinatraServer {
  import scala.concurrent.ExecutionContext.Implicits.global

  val cluster = Cluster.builder()
    .addContactPoint("127.0.0.1")
    .build()
  implicit val session = cluster.connect()

  register(new RootController)
  register(new LoginController)
}
