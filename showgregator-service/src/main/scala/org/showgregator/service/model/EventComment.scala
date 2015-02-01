package org.showgregator.service.model

import org.joda.time.DateTime
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.{Ascending, ClusteringOrder, PrimaryKey, PartitionKey}
import com.datastax.driver.core.Row
import com.twitter.util.Future

// a comment's threadReplyId is a combination of two values:
// a thread ID (the upper four bytes) and a reply ID
// (the lower four bytes).

case class EventComment(event: UUID,
                        thread: Int,
                        reply: Int,
                        inReplyTo: Option[Int],
                        author: Option[String],
                        date: Option[DateTime],
                        comment: Option[String],
                        isDeleted: Boolean) {
}

sealed class EventCommentRecord extends CassandraTable[EventCommentRecord, EventComment] {
  object event extends UUIDColumn(this) with PartitionKey[UUID]
  object thread extends IntColumn(this) with PrimaryKey[Int] with ClusteringOrder[Int] with Descending
  object reply extends IntColumn(this) with PrimaryKey[Int] with ClusteringOrder[Int] with Descending
  object inReplyTo extends OptionalIntColumn(this)
  object author extends OptionalStringColumn(this)
  object date extends OptionalDateTimeColumn(this)
  object comment extends OptionalStringColumn(this)
  object isDeleted extends BooleanColumn(this)

  def fromRow(r: Row): EventComment = EventComment(event(r), thread(r), reply(r),
    inReplyTo(r), author(r), date(r), comment(r), isDeleted(r))
}

object EventCommentRecord extends EventCommentRecord with Connector {
  override val tableName = "event_comments"

  def commentsFor(event: UUID)(implicit session:Session): Future[Seq[EventComment]] = {
    select.where(_.event eqs event)
      .orderBy(_.thread.desc)
      .orderBy(_.reply.desc)
      .collect()
  }

  def nextThreadId(event: UUID)(implicit session:Session): Future[Int] = {
    select.where(_.event eqs event)
      .get()
      .map(r => r.map(_.thread).getOrElse(0))
  }

  def nextReplyId(event: UUID, thread: Int)(implicit session:Session): Future[Int] = {
    select.where(_.event eqs event)
      .and(_.thread eqs thread)
      .get()
      .map(r => r.map(_.reply).getOrElse(0))
  }

  def insertComment(comment: EventComment)(implicit session:Session): Future[ResultSet] = {
    insert.ifNotExists()
      .value(_.event, comment.event)
      .value(_.thread, comment.thread)
      .value(_.reply, comment.reply)
      .value(_.inReplyTo, comment.inReplyTo)
      .value(_.author, comment.author)
      .value(_.date, comment.date)
      .value(_.comment, comment.comment)
      .value(_.isDeleted, comment.isDeleted)
      .execute()
  }

  /**
   * To delete a comment, we simply overwrite the comment where all fields
   * are blank, and the deleted flag is true. We keep the comment tree intact.
   *
   * @param event
   * @param thread
   * @param reply
   * @param inReplyTo
   * @return
   */
  def deleteComment(event: UUID, thread: Int, reply: Int, inReplyTo: Option[Int])(implicit session:Session): Future[ResultSet] = {
    insert.value(_.event, event)
      .value(_.thread, thread)
      .value(_.reply, reply)
      .value(_.inReplyTo, inReplyTo)
      .value(_.author, None)
      .value(_.date, None)
      .value(_.comment, None)
      .value(_.isDeleted, true)
      .execute()
  }
}