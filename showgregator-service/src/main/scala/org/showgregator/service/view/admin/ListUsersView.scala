package org.showgregator.service.view.admin

import com.twitter.finatra.View
import org.showgregator.service.model.User

/**
 * Created by cmarshall on 1/29/15.
 */
class ListUsersView(val users:List[User], val prevoffset:String, val nextoffset:String) extends View {
  override def template: String = "templates/admin/listusers.mustache"
}
