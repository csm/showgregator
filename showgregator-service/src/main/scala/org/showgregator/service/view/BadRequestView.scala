package org.showgregator.service.view

import com.twitter.finatra.View

/**
 * Created with IntelliJ IDEA.
 * User: cmarshall
 * Date: 1/25/15
 * Time: 6:51 PM
 * To change this template use File | Settings | File Templates.
 */
class BadRequestView(val message:String = "The fuck did you mean by that?") extends View {
  def template: String = "templates/400.mustache"
}
