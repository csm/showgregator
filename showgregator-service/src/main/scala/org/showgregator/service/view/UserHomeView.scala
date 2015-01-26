package org.showgregator.service.view

import com.twitter.finatra.View
import org.showgregator.service.model.{BaseUser, TransientUser, User}
import java.security.MessageDigest

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 8:38 PM
 * To change this template use File | Settings | File Templates.
 */
class UserHomeView(user: BaseUser) extends View {
  val handle = user match {
    case u:User => u.handle.getOrElse(u.email)
    case u:TransientUser => u.email
  }
  val gravatarHash = {
    val md = MessageDigest.getInstance("MD5")
    md.update(user.email.trim.toLowerCase.getBytes("UTF-8"))
    md.digest().map { b => "%02x".format(b & 0xff)}.mkString
  }

  def template: String = "templates/home.mustache"
}
