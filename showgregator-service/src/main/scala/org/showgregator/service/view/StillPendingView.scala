package org.showgregator.service.view

import com.twitter.finatra.View

class StillPendingView(val email: String) extends View {
  def template: String = "templates/stillpending.mustache"
}
