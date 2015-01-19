package org.showgregator.service.model

import java.util.UUID

import com.datastax.driver.core.{Row, BoundStatement, Session}

import scala.util.parsing.json.JSONObject

/**
 * Created by cmarshall on 1/18/15.
 */

object Calendar {
  def fromJson(obj: JSONObject):Calendar = {
    new Calendar(obj.obj.get("id") match {
      case Some(s:String) => UUID.fromString(s)
      case _ => throw new IllegalArgumentException
    }, obj.obj.get("name") match {
      case Some(s:String) => s
      case _ => throw new IllegalArgumentException
    })
  }

  def fromRow(row: Row): Calendar = {
    new Calendar(row.getUUID("id"), row.getString("name"))
  }
}

case class Calendar(id: UUID, name: String) extends JsonConvertible with CassandraConvertible {
  override def toJson = {
    JSONObject(Map("id" -> id.toString, "name" -> name))
  }

  override def insertStatement(session: Session):BoundStatement = {
    session.prepare("INSERT INTO calendars (id, name) VALUES (?, ?);").bind(id, name)
  }

  override def deleteStatement(session: Session):BoundStatement = {
    session.prepare("DELETE FROM calendars WHERE id = ?;").bind(id)
  }
}

