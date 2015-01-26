package org.showgregator.service.view

import com.twitter.finatra.View

class ServerErrorView(val message: String, val stacktrace: String = "") extends View {
  def template: String = "templates/500.mustache"
}
