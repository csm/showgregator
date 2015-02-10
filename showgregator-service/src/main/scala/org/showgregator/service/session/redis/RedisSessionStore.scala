package org.showgregator.service.session.redis

import com.twitter.logging.Logger
import com.twitter.util.Future
import org.showgregator.core.geo.USLocales.City
import org.showgregator.core.util.Bytes
import org.showgregator.service.session.{Session, SessionStore}
import scredis._
import java.util.UUID
import scala.concurrent.ExecutionContext
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{Duration, DateTime}
import org.showgregator.service.model.{TransientUser, User, BaseUser}
import java.io.ByteArrayOutputStream
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import scredis.serialization.{BytesWriter, BytesReader}
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper

object RedisSessionStore {
  val kryo = new Kryo()
  val log = Logger("finatra")

  def freezeSession(s:Session):Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    val out = new Output(bout)
    kryo.writeObject(out, s.id)
    kryo.writeObject(out, s.createTime.getMillis)
    s.user match {
      case u:User => {
        kryo.writeObject(out, true)
        kryo.writeObject(out, u.id)
        kryo.writeObject(out, u.email)
        u.handle match {
          case Some(h) => {
            kryo.writeObject(out, true)
            kryo.writeObject(out, h)
          }
          case None =>
            kryo.writeObject(out, false)
        }
        u.city match {
          case Some(c) =>
            kryo.writeObject(out, true)
            kryo.writeObject(out, c)
          case None =>
            kryo.writeObject(out, false)
        }
        u.timeZoneId match {
          case Some(tz) =>
            kryo.writeObject(out, true)
            kryo.writeObject(out, tz)
          case None =>
            kryo.writeObject(out, false)
        }
      }
      case u:TransientUser => {
        kryo.writeObject(out, false)
        kryo.writeObject(out, u.email)
        kryo.writeObject(out, u.userId)
      }
    }
    out.close()
    bout.toByteArray
  }

  def unfreezeSession(a:Array[Byte]):Session = {
    log.debug("unfreezing: %s", Bytes(a))
    val input = new Input(a)
    val id = kryo.readObject(input, classOf[UUID])
    val t = new DateTime(kryo.readObject(input, classOf[Long]))
    val isLoggedIn = kryo.readObject(input, classOf[Boolean])
    val user = if (isLoggedIn) {
      val uid = kryo.readObject(input, classOf[UUID])
      val email = kryo.readObject(input, classOf[String])
      val hasHandle = kryo.readObject(input, classOf[Boolean])
      val handle = if (hasHandle) {
        Some(kryo.readObject(input, classOf[String]))
      } else {
        None
      }
      val city = kryo.readObject(input, classOf[Boolean]) match {
        case true => Some(kryo.readObject(input, classOf[City]))
        case false => None
      }
      val tz = kryo.readObject(input, classOf[Boolean]) match {
        case true => Some(kryo.readObject(input, classOf[String]))
        case false => None
      }
      User(uid, email, handle, city = city, timeZoneId = tz)
    } else {
      val email = kryo.readObject(input, classOf[String])
      val userId = kryo.readObject(input, classOf[UUID])
      TransientUser(email, userId)
    }
    Session(id, user, t, t)
  }
}

/**
 * Session store on Redis.
 */
class RedisSessionStore(redises: Array[Redis], ttl: Duration = Duration.standardHours(1))(implicit context: ExecutionContext) extends SessionStore {
  import RedisSessionStore._
  val DateFormatter = ISODateTimeFormat.basicDateTime()

  private def redis(key: UUID): Redis = {
    redises((Math.abs(key.getLeastSignificantBits) % redises.length).toInt)
  }

  implicit val reader = BytesReader
  def get(id: UUID): Future[Option[Session]] = {
    val sId = s"session.${id.toString}"
    for {
      map:Option[Array[Byte]] <- redis(id).get[Array[Byte]](sId).asFinagle
      ttl:Either[Boolean, Long] <- map match {
        case Some(m) => redis(id).pTtl(sId).asFinagle
        case None => Future.value(Left(false))
      }
    } yield {
      (map, ttl) match {
        case (Some(a), Right(t)) => {
          try {
            val s = unfreezeSession(a)
            Some(s.expires(DateTime.now().plusMillis(t.toInt)))
          } catch {
            case t: Throwable => {
              log.warning(t, "error unfreezing session")
              None
            }
          }
        }
        case _ => None
      }
    }
  }

  implicit val writer = BytesWriter
  def put(session: Session): Future[Boolean] = {
    val id = s"session.${session.id.toString}"
    val a = freezeSession(session)
    redis(session.id).set(id, a).flatMap(_ => redis(session.id).pExpire(id, ttl.getMillis)).asFinagle
  }

  def delete(id: UUID): Future[Boolean] = {
    redis(id).del(s"session.${id.toString}").map(_ == 1).asFinagle
  }

  def extend(id: UUID): Future[Boolean] = {
    redis(id).pExpire(s"session.${id.toString}", ttl.getMillis).asFinagle
  }
}
