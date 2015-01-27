package org.showgregator.core

import java.util.UUID

object UUIDs {
  def parseUUID(idString: Option[String]): Option[UUID] = {
    idString.flatMap(id =>
      try {
        Some(UUID.fromString(id))
      } catch {
        case iae: IllegalArgumentException => None
        case npe: NullPointerException => None
      })
  }
}
