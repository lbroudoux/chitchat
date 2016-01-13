package models

import play.api.libs.json._
/**
 * @author laurent
 */
case class ChitChatModel(author: String, text: String, thread: Option[Int], var createdAt: Option[Long])

object ChitChatModel {
  implicit val userReads = Json.reads[ChitChatModel]
}
