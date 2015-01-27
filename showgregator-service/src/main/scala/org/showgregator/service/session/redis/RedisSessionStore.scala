package org.showgregator.service.session.redis

import org.showgregator.service.session.{Session, SessionStore}
import scredis._
import java.util.UUID
import scala.concurrent.{Future, ExecutionContext}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{Duration, DateTime}
import org.showgregator.service.model.{TransientUser, User, BaseUser}
import java.io.ByteArrayOutputStream
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import scredis.serialization.{BytesWriter, BytesReader}

/**
 * Session store on Redis.
 */
class RedisSessionStore(redis: Redis, ttl: Duration = Duration.standardHours(1))(implicit context: ExecutionContext) extends SessionStore {

  val DateFormatter = ISODateTimeFormat.basicDateTime()
  val kryo = new Kryo()

  def freezeSession(s:Session):Array[Byte] = {
    val bout = new ByteArrayOutputStream()
    val out = new Output(bout)
    kryo.writeObject(out, s.id)
    kryo.writeObject(out, s.createTime.getMillis)
    s.user match {
      case u:User => {
        kryo.writeObject(out, true)
        kryo.writeObject(out, u.email)
        u.handle match {
          case Some(h) => {
            kryo.writeObject(out, true)
            kryo.writeObject(out, h)
          }
          case None =>
            kryo.writeObject(out, false)
        }
      }
      case u:TransientUser => {
        kryo.writeObject(out, false)
        kryo.writeObject(out, u.email)
      }
    }
    bout.toByteArray
  }

  def unfreezeSession(a:Array[Byte]):Session = {
    val input = new Input(a)
    val id = kryo.readObject(input, classOf[UUID])
    val t = new DateTime(kryo.readObject(input, classOf[Long]))
    val isLoggedIn = kryo.readObject(input, classOf[Boolean])
    val user = if (isLoggedIn) {
      val email = kryo.readObject(input, classOf[String])
      val hasHandle = kryo.readObject(input, classOf[Boolean])
      val handle = if (hasHandle) {
        Some(kryo.readObject(input, classOf[String]))
      } else {
        None
      }
      User(email, handle)
    } else {
      val email = kryo.readObject(input, classOf[String])
      TransientUser(email)
    }
    Session(id, user, t, t)
  }

  implicit val reader = BytesReader
  def get(id: UUID): Future[Option[Session]] = {
    val sId = id.toString
    for {
      map:Option[Array[Byte]] <- redis.get[Array[Byte]](sId)
      ttl:Either[Boolean, Long] <- map match {
        case Some(m) => redis.pTtl(sId)
        case None => Future.successful(Left(false))
      }
    } yield {
      (map, ttl) match {
        case (Some(a), Right(t)) => {
          val s = unfreezeSession(a)
          Some(s.expires(DateTime.now().plusMillis(t.toInt)))
        }
        case _ => None
      }
    }
  }

  implicit val writer = BytesWriter
  def put(session: Session): Future[Boolean] = {
    val id = session.id.toString
    val a = freezeSession(session)
    redis.set(id, a).flatMap(_ => redis.pExpire(id, ttl.getMillis))
  }

  def delete(id: UUID): Future[Boolean] = {
    redis.del(id.toString).map(_ == 1)
  }

  def extend(id: UUID): Future[Boolean] = {
    redis.pExpire(id.toString, ttl.getMillis)
  }
}
