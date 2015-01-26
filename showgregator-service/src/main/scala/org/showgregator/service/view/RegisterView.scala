package org.showgregator.service.view

import com.twitter.finatra.View

class RegisterView(val token:String, val email:String="") extends View {
  def template: String = "templates/register.mustache"
}
