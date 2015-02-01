package org.showgregator.service.controller

import java.util.UUID

import com.datastax.driver.core.Session
import org.showgregator.core.PasswordHashing
import org.showgregator.service.admin._
import org.showgregator.service.model._
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
        UserRecord.select.allowFiltering().where(_.email gte offset.get).limit(50).fetch().asFinagle
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
        User.createUser(UUID.randomUUID(), email.get, strParam(request.params.get("handle")), password.get.toCharArray)
          .flatMap({
            case Some(user) => render.view(new AddedUserView(user.id)).toFuture
            case None => throw new IllegalArgumentException
          })
      } else throw new IllegalArgumentException
    }
  }

  get("/admin/edituser/:id") {
    !!!(new EditUserPermission)(_) { request =>
      val id = UUID.fromString(request.routeParams.get("id").get)
      UserRecord.getByID(id).flatMap({
        case Some(user) => render.view(new EditUserView(user)).toFuture
        case None => render.view(new NotFoundView(s"user $id")).notFound.toFuture
      })
    }
  }

  post("/admin/edituser/:id") {
    !!!(new EditUserPermission)(_) { request =>
      val id = UUID.fromString(request.routeParams.get("id").get)
      UserRecord.getByID(id).flatMap({
        case Some(user) => if (nonEmpty(request.params.get("email"))) {
          User.updateUserEmail(user, request.params.get("email").get).flatMap({
            case Some(edited) => render.view(new EditUserView(edited, "<strong>USER EMAIL CHANGED</strong>")).toFuture
            case None => render.view(new ServerErrorView("whups, failed to edit the user")).toFuture
          })
        } else if (request.params.get("handle").isDefined) {
          User.updateUserHandle(user, strParam(request.params.get("handle"))).flatMap({
            case Some(edited) => render.view(new EditUserView(edited, "<strong>USER HANDLE CHANGED</strong>")).toFuture
            case None => render.view(new ServerErrorView("whups, failed to edit the user")).toFuture
          })
        } else if (nonEmpty(request.params.get("password"))) {
          User.updateUserPassword(user, request.params.get("password").get.toCharArray).flatMap({
            case Some(edited) => render.view(new EditUserView(edited, "<strong>USER PASSWORD CHANGED</strong>")).toFuture
            case None => render.view(new ServerErrorView("whups, failed to edit the user")).toFuture
          })
        } else {
          render.view(new EditUserView(user)).toFuture
        }
        case None => render.view(new NotFoundView(s"user $id")).notFound.toFuture
      })
    }
  }

  post("/admin/deleteuser/:id") {
    !!!(new DeleteUserPermission)(_) { request =>
      throw new UnsupportedOperationException("not yet implemented")
    }
  }

  get("/admin/addpending") {
    !!!(new AddPendingUserPermission)(_) { request =>
      render.static("html/admin/addpending.html").toFuture
    }
  }

  get("/admin/addtransient") {
    !!!(new AddPendingUserPermission)(_) { request =>
      render.static("html/admin/addtransient.html").toFuture
    }
  }

  def nonEmpty(s:Option[String]):Boolean = s.isDefined && s.get.length > 0
  def strParam(s:Option[String]):Option[String] = s.flatMap(ss => if (ss.trim.length > 0) Some(ss.trim) else None)
}
