package org.showgregator.service

import com.twitter.finatra.FinatraServer
import org.showgregator.service.admin.DefaultAdminAuthStore
import org.showgregator.service.controller._
import com.datastax.driver.core.Cluster
import org.showgregator.service.filters.AccessFilter
import org.showgregator.service.session.{SessionStore, DefaultSessionStore}
import org.showgregator.service.session.redis.RedisSessionStore
import scredis.Redis
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.config.ConfigException.Missing

object ShowgregatorServer extends FinatraServer {
  import scala.concurrent.ExecutionContext.Implicits.global

  log.info("env: %s", com.twitter.finatra.config.env)
  val config = ConfigFactory.load().getConfig("showgregator")
  val sessionStorage = try {
    config.getString("session.storage")
  } catch {
    case missing:Missing => "default"
  }
  val cassandraHost = try {
    config.getString("cassandra.host")
  } catch {
    case missing:Missing => "127.0.0.1"
  }

  log.info("connecting to cassandra host %s", cassandraHost)
  val cluster = Cluster.builder()
    .addContactPoint(cassandraHost)
    .build()
  implicit val session = cluster.connect("showgregator")
  val store:SessionStore = sessionStorage match {
    case "redis" => new RedisSessionStore(Array(Redis()))
    case _ => new DefaultSessionStore
  }
  log.info("using session store %s (flag: %s)", store, sessionStorage)
  implicit val sessionStore = store

  log.info("running on java version: %s", System.getProperty("java.version"))

  addFilter(new AccessFilter)

  register(new RootController)
  register(new LoginController)
  register(new RegisterController)
  register(new VerifyEmailController)
  register(new UserController)
  register(new CalendarController)

  implicit val adminStore = new DefaultAdminAuthStore
  register(new AdminController)
}
