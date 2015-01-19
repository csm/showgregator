package org.showgregator.service.model

import java.util.UUID
import org.joda.time.DateTime

/**
 * Created by cmarshall on 1/18/15.
 */
case class Event(calendar: Calendar, id: UUID, when: DateTime, title: String, venue: Venue)
  extends JsonConvertible
{

}
