package org.showgregator.service.view

import org.showgregator.service.model.{TransientUser, User, BaseUser}
import com.twitter.finatra.View

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/27/15
 * Time: 10:29 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class BaseUserView(val user: BaseUser) extends View {
  val logout_links = user match {
    case u:User => s"""<span id="logout"><a href="/account">${user.email}</a><br><a href="/logout">Logout</a></span>"""
    case u:TransientUser => s"""<span id="logout">${user.email}<br><a href="/register/${u.id.toString}">Register</a></span>"""
  }

  val navbar_links = user match {
    case u:User => """<a href="/calendar/mine/today">My Calendar</a> | <a href="/calendars">Shared Calendars</a> | <a href="/inbox">Messages</a>"""
    case u:TransientUser => """<a href="/calendars">Shared Calendars</a>"""
  }
}
