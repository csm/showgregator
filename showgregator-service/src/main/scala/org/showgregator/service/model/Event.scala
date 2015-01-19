package org.showgregator.service.model

import java.util.UUID
import com.datastax.driver.core.Session
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.Success
import scala.util.parsing.json.JSONObject

case class Event(calendarId: UUID, id: UUID, when: DateTime, title: String, venueId: UUID) extends JsonConvertible {
  @volatile private var _calendar:Option[Calendar] = None

  override def toJson: JSONObject = JSONObject(Map(
    "calendar" -> calendarId.toString,
    "id" -> id.toString,
    "when" -> when.toString("YYYY-MM-dd HH:mm:ssZZZ"),
    "title" -> title,
    "venue" -> venueId.toString
  ))

  def calendar()(implicit session: Session):Future[Calendar] = {
    if (_calendar.isEmpty) {
      Calendar.fromCassandra(session, calendarId).andThen({
        case Success(c) => _calendar = Some(c)
      })
    } else Future.successful(_calendar.get)
  }
}
