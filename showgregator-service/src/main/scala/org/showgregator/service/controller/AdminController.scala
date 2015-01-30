package org.showgregator.service.controller

import java.util.UUID

import com.datastax.driver.core.Session
import org.showgregator.core.PasswordHashing
import org.showgregator.service.admin.{EditUserPermission, ReadPermission, AddUserPermission, AdminAuthStore}
import org.showgregator.service.model.{UserRecord, User}
import org.showgregator.service.finagle.FinagleFutures.ScalaFutureWrapper
import org.showgregator.service.view.{ServerErrorView, NotFoundView}
import org.showgregator.service.view.admin.{EditUserView, ListUsersView, AddedUserView}
import com.websudos.phantom.Implicits._

import scala.concurrent.ExecutionContext

class AdminController(implicit val session: Session,
                      implicit val context: ExecutionContext,
                      implicit override val adminStore: AdminAuthStore)
  extends AdminControllerBase {

  get("/admin") {
    !!!(new ReadPermission)(_) { request =>
      render.static("html/admin/index.html").toFuture
    }
  }

  get("/admin/listusers") {
    !!!(new ReadPermission)(_) { request =>
      val offset = request.params.get("o")
      val future = if (offset.isDefined) {
        UserRecord.select.allowFiltering.where(_.email gte offset.get).limit(50).fetch().asFinagle
      } else {
        UserRecord.select.limit(50).fetch().asFinagle
      }

      future.flatMap(users => {
        val prev = users.head
        val next = users.last
        render.view(new ListUsersView(users.toList, prev.email, next.email)).toFuture
      })
    }
  }

  get("/admin/adduser") {
    !!!(new AddUserPermission)(_) { request =>
      render.static("html/admin/adduser.html").toFuture
    }
  }

  post("/admin/adduser") {
    !!!(new AddUserPermission)(_) { request =>
      val email = request.params.get("email").map(_.trim)
      val password = request.params.get("password")
      if (nonEmpty(email) && nonEmpty(password)) {
        val user = User(UUID.randomUUID(), email.get, strParam(request.params.get("handle")),
          PasswordHashing(password.get.toCharArray))
        UserRecord.insertUser(user).asFinagle.flatMap(rs =>
          if (rs.wasApplied())
            render.view(new AddedUserView(user.id)).toFuture
          else
            throw new IllegalArgumentException
        )
      } else throw new IllegalArgumentException
    }
  }

  get("/admin/edituser/:id") {
    !!!(new EditUserPermission)(_) { request =>
      val id = UUID.fromString(request.routeParams.get("id").get)
      UserRecord.getByID(id).asFinagle.flatMap({
        case Some(user) => render.view(new EditUserView(user)).toFuture
        case None => render.view(new NotFoundView(s"user $id")).notFound.toFuture
      })
    }
  }

  post("/admin/edituser/:id") {
    !!!(new EditUserPermission)(_) { request =>
      val id = UUID.fromString(request.routeParams.get("id").get)
      UserRecord.getByID(id).asFinagle.flatMap({
        case Some(user) => if (request.params.get("email").isDefined) {
          User.updateUserEmail(user, request.params.get("email").get).asFinagle.flatMap({
            case Some(edited) => render.view(new EditUserView(edited, "<strong>USER EMAIL CHANGED</strong>")).toFuture
            case None => render.view(new ServerErrorView("whups, failed to edit the user")).toFuture
          })
        } else if (request.params.get("handle").isDefined) {
          throw new UnsupportedOperationException("not yet implemented")
        } else if (request.params.get("password").isDefined) {
          throw new UnsupportedOperationException("not yet implemented")
        } else {
          render.view(new EditUserView(user)).toFuture
        }
        case None => render.view(new NotFoundView(s"user $id")).notFound.toFuture
      })
    }
  }

  def nonEmpty(s:Option[String]):Boolean = s.isDefined && s.get.length > 0
  def strParam(s:Option[String]):Option[String] = s.flatMap(ss => if (ss.trim.length > 0) Some(ss.trim) else None)
}
