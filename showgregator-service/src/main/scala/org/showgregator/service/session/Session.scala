package org.showgregator.service.session

import java.util.UUID
import org.joda.time.DateTime
import org.showgregator.service.model.BaseUser

/**
 * A user session.
 *
 * Parameters include:
 *
 * @param id The session ID, randomly generated.
 * @param user The user object.
 * @param createTime When this session was created.
 * @param expireTime When this session will expire.
 */
case class Session(id: UUID,
                   user: BaseUser,
                   createTime: DateTime,
                   expireTime: DateTime) {
  def isValid: Boolean = DateTime.now().isBefore(expireTime)

  def expires(newExpireTime: DateTime) = Session(id, user, createTime, newExpireTime)
}