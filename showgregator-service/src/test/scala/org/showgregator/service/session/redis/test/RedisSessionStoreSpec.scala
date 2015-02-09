package org.showgregator.service.session.redis.test

import java.util.UUID

import org.joda.time.DateTime
import org.scalatest.{Matchers, FlatSpec}
import org.showgregator.service.model.{TransientUser, PendingUser, User}
import org.showgregator.service.session.Session
import org.showgregator.service.session.redis.RedisSessionStore

class RedisSessionStoreSpec extends FlatSpec with Matchers {
  "serialize and deserialize session" should "produce the same value" in {
    val user = User(UUID.randomUUID(), "user@domain.com", Some("Joe User"), city = None, timeZoneId = None)
    val session = Session(UUID.randomUUID(), user, DateTime.now(), DateTime.now())
    val serial = RedisSessionStore.freezeSession(session)
    val session2 = RedisSessionStore.unfreezeSession(serial)
    session2.id should be (session.id)
    session2.user.email should be (session.user.email)
    session2.user.isInstanceOf[User] should be (true)
    session2.user.asInstanceOf[User].handle should be (user.handle)
  }

  "serialize and deserialize session without handle" should "produce the same value" in {
    val user = User(UUID.randomUUID(), "user@domain.com", None, city = None, timeZoneId = None)
    val session = Session(UUID.randomUUID(), user, DateTime.now(), DateTime.now())
    val serial = RedisSessionStore.freezeSession(session)
    val session2 = RedisSessionStore.unfreezeSession(serial)
    session2.id should be (session.id)
    session2.user.email should be (session.user.email)
    session2.user.isInstanceOf[User] should be (true)
    session2.user.asInstanceOf[User].handle should be (user.handle)
  }

  "serialize and deserialize transient user session" should "produce the same value" in {
    val user = TransientUser("transient@domain.com", UUID.randomUUID())
    val session = Session(UUID.randomUUID(), user, DateTime.now(), DateTime.now())
    val serial = RedisSessionStore.freezeSession(session)
    val session2 = RedisSessionStore.unfreezeSession(serial)
    session2.id should be (session.id)
    session2.user.email should be (user.email)
    session2.user.isInstanceOf[TransientUser] should be (true)
  }
}
