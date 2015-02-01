package org.showgregator.service.view

import com.twitter.finatra.View

class RegisterView(val id:Option[String], val email:String="") extends View {
  val token = id.map(t => s"/$t").getOrElse("")
  def template: String = "templates/register.mustache"
}
