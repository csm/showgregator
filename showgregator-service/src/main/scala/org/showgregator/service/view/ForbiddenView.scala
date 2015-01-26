package org.showgregator.service.view

import com.twitter.finatra.View

class ForbiddenView(val message: String = "Forbidden.") extends View {
  def template: String = "templates/403.mustache"
}
