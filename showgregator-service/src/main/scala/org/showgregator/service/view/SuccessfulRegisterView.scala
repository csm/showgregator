package org.showgregator.service.view

import com.twitter.finatra.View

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 7:06 PM
 * To change this template use File | Settings | File Templates.
 */
class SuccessfulRegisterView(val email: String) extends View {
  def template: String = "templates/registered.mustache"
}
