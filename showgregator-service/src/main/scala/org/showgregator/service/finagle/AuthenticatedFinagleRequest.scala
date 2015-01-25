package org.showgregator.service.finagle

import com.twitter.finagle.http.Request
import org.showgregator.service.model.User

case class AuthenticatedFinagleRequest(request: Request, user: User)