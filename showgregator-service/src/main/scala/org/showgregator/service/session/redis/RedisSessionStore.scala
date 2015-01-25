package org.showgregator.service.session.redis

import org.showgregator.service.session.{Session, SessionStore}
import scredis.Redis
import java.util.UUID
import scala.concurrent.{Future, ExecutionContext}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{Duration, DateTime}

/**
 * Session store on Redis.
 */
class RedisSessionStore(redis: Redis, ttl: Duration = Duration.standardHours(1))(implicit context: ExecutionContext) extends SessionStore {

  val DateFormatter = ISODateTimeFormat.basicDateTime()

  def get(id: UUID): Future[Option[Session]] = {
    val sId = id.toString
    for {
      map:Option[Map[String, String]] <- redis.hGetAll[String](sId)
      ttl:Either[Boolean, Long] <- map match {
        case Some(m) => redis.pTtl(sId)
        case None => Future.successful(Left(false))
      }
    } yield {
      (map, ttl) match {
        case (Some(m), Right(t)) => (m.get("e"), m.get("l"), m.get("c")) match {
          case (Some(email), Some(loggedIn), Some(created)) =>
            val date = DateFormatter.parseDateTime(created)
            Some(Session(id, email, "t".equals(loggedIn), date, DateTime.now().plusMillis(t.toInt)))
          case _ => None
        }
        case _ => None
      }
    }
  }

  def put(session: Session): Future[Boolean] = {
    val id = session.id.toString
    val map = Map("e" -> session.email,
                  "l" -> (if (session.loggedIn) { "t" } else { "f" }),
                  "c" -> session.createTime.toString(DateFormatter))
    redis.hmSet(id, map).flatMap(_ => redis.pExpire(id, ttl.getMillis))
  }

  def delete(id: UUID): Future[Boolean] = {
    redis.del(id.toString).map(_ == 1)
  }

  def extend(id: UUID): Future[Boolean] = {
    redis.pExpire(id.toString, ttl.getMillis)
  }
}
