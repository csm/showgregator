package org.showgregator.service.admin

import java.security.Permission

import com.twitter.logging.Logger
import com.twitter.util.Future

class DefaultAdminAuthStore extends AdminAuthStore {
  val log = Logger("finatra")

  override def authenticate(user: String, password: String): Future[Option[AdminUser]] = {
    log.info("authenticate %s:%s", user, password)
    if (user.equals("admin") && password.equals("admin")) {
      log.info("returning admin user")
      Future.value(Some(new AdminUser("admin") {
        override def hasPermission(permission: Permission): Future[Boolean] = {
          log.info("returning true, has permission %s", permission)
          Future.value(true)
        }
      }))
    } else {
      Future.value(None)
    }
  }
}
