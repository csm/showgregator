package org.showgregator.service.admin

import java.security.Permission

import com.twitter.util.Future

abstract class AdminUser(val name: String) {
  def hasPermission(permission:Permission): Future[Boolean]
}

trait AdminAuthStore {
  def authenticate(user: String, password: String): Future[Option[AdminUser]]
}
