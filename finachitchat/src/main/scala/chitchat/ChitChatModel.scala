package chitchat

/**
 * @author laurent
 */
case class ChitChatModel(author: String, text: String, thread: Option[Int], var createdAt: Option[Long])
