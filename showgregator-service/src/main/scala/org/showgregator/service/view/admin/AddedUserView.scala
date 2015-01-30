package org.showgregator.service.view.admin

import java.util.UUID

import com.twitter.finatra.View

/**
 * Created by cmarshall on 1/29/15.
 */
class AddedUserView(val userid: UUID) extends View {
  override def template: String = "templates/admin/addeduser.mustache"
}
