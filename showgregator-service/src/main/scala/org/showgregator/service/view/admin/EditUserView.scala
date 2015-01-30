package org.showgregator.service.view.admin

import com.twitter.finatra.View
import org.showgregator.service.model.User

/**
 * Created by cmarshall on 1/29/15.
 */
class EditUserView(user:User, val did_edit:String = "") extends View {
  override def template: String = "templates/admin/edituser.mustache"
  val id = user.id
  val email = user.email
  val handle = user.handle.getOrElse("")
}
