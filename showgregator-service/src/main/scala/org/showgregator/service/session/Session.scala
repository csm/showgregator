package org.showgregator.service.session

import java.util.UUID
import org.joda.time.DateTime

/**
 * A user session.
 *
 * Parameters include:
 *
 * @param id The session ID, randomly generated.
 * @param email The user's email.
 * @param loggedIn Will be true if this is a user logged in to his account (otherwise, this is a transient user visiting via a link).
 * @param createTime When this session was created.
 * @param expireTime When this session will expire.
 */
case class Session(id: UUID,
                   email: String,
                   loggedIn: Boolean,
                   createTime: DateTime,
                   expireTime: DateTime) {
  def isValid: Boolean = DateTime.now().isBefore(expireTime)
}