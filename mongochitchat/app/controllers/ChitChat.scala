package controllers

import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.api._

import models.ChitChatModel
/**
 * @author laurent
 */
object ChitChat extends Controller with MongoController {

  val idPruner = (__ \ '_id).json.prune

  def collection: JSONCollection = db.collection[JSONCollection]("chitchat")

  def chitchat() = Action(parse.json) { request =>
    val chitchatM: ChitChatModel = request.body.validate[ChitChatModel].get
    chitchatM.createdAt = Some(System.currentTimeMillis())
    collection.insert(chitchatM).map { lastError =>
      Logger.debug(s"Successfully inserted with LastError: $lastError")
    }
    Created
  }

  def readLatest(superhero: String) = Action.async { request =>
    // Let's do our Mongo exemple object query.
    val cursor: Cursor[JsObject] = collection
      .find(Json.obj("author" -> superhero))
      .sort(Json.obj("createdAt" -> -1))
      .cursor[JsObject]

    // Gather all the JsObjects in a list.
    val futureChitchatList: Future[List[JsObject]] = cursor.collect[List](1)

    // Everything's ok! Let's reply with the first result.
    futureChitchatList.map { chitchats =>
      Ok(chitchats(0))
    }
  }

  def readThread(threadId: Int) = Action.async { request =>
    // Let's do our Mongo exemple object query.
    val cursor: Cursor[JsObject] = collection
        .find(Json.obj("thread" -> threadId))
        .sort(Json.obj("createdAt" -> 1))
        .cursor[JsObject]

    // Gather all the objects into a List and transform into a JsArray.
    extractJsArray(cursor.collect[List]()).map { chitchats =>
      Ok(chitchats(0))
    }
  }

  def searchChitchat(q: String, l: Int) = Action.async { request =>
    // Let's do our Mongo exemple object query.
    val cursor: Cursor[JsObject] = collection
      .find(Json.obj("$text" -> Json.obj("$search" -> q) ))
      .cursor[JsObject]

    // Gather all the objects into a List and transform into a JsArray.
    extractJsArray(cursor.collect[List](l)).map { chitchats =>
      Ok(chitchats(0))
    }
  }

  /** Transform a List of JsObject into a JsArray. */
  def extractJsArray(futureChitchatList: Future[List[JsObject]]): Future[JsArray] = {
    // Transform the list into a JsArray and remove _id.
    futureChitchatList.map { chitchats =>
      Json.arr(chitchats.map(_.transform(idPruner).get))
    }
  }
}
