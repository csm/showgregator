package org.showgregator.service.view

import com.twitter.finatra.View

class NotFoundView(val uri:String) extends View {
  def template: String = "templates/404.mustache"
}
