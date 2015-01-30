package org.showgregator.service.admin

import java.io.File
import java.security.{MessageDigest, Permission}

import com.twitter.util.Future
import org.apache.commons.codec.binary.Base64
import org.showgregator.core.PasswordHashing

import scala.concurrent.ExecutionContext
import scala.io.Source

class PasswordFileUser(perms:String, name: String) extends AdminUser(name) {
  val permissions = perms.split("|").map(Permissions.forName)

  override def hasPermission(permission: Permission): Future[Boolean] = Future.value(permissions.exists(p => p.implies(permission)))
}

class PasswordFileAuthStore(val file:File)(implicit val context: ExecutionContext) extends AdminAuthStore {
  private val users = {
    val input = Source.fromFile(file)
    val result = input.getLines()
      .filter(line => !line.trim.startsWith("#"))
      .map(line => line.split(":"))
      .filter(parts => parts.length == 4)
      .map(parts => parts(0) -> (parts(1), parts(2), parts(3))).toMap
    input.close()
    result
  }

  override def authenticate(user: String, password: String): Future[Option[AdminUser]] = {
      users.get(user) match {
        case Some(u) => {
          val h = Base64.decodeBase64(u._2)
          val s = Base64.decodeBase64(u._1)
          val hash = PasswordHashing(password.toCharArray, salt = Some(s))
          if (MessageDigest.isEqual(hash.hash, h)) {
            Future.value(Some(new PasswordFileUser(u._3, user)))
          } else {
            Future.value(None)
          }
        }
        case None => {
          PasswordHashing(password.toCharArray)
          Future.value(None)
        }
      }
    }
  }
}
