package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._

import playlastik.RestClient

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.ObjectSource
import org.elasticsearch.search.sort.SortOrder

import models.ChitChatModel
/**
 * @author laurent
 */
object ChitChat extends Controller {

  val hitsExtractor = (__ \ 'hits \ 'hits).json.pick[JsArray]

  val sourceExtractor = (__ \ '_source).json.pick

  def chitchat() = Action(parse.json) { request =>
    val chitchatM: ChitChatModel = request.body.validate[ChitChatModel].get
    chitchatM.createdAt = Some(System.currentTimeMillis())
    RestClient.index { index into "chitchat/chitchat" doc ObjectSource(chitchatM) }
    Created
  }

  def readLatest(superhero: String) = Action.async { request =>
    // Should be faster because does not compute scoring.
    // (see http://www.elasticsearch.org/guide/en/elasticsearch/guide/current/_finding_exact_values.html)
    val rep = RestClient.search {
      search in "chitchat"->"chitchat" query { matchall } filter { termFilter("author", superhero) } sort {
        by field "createdAt" order(SortOrder.DESC)
      } limit 1
    }
    rep.map(
      resp => {
        Logger.debug("readLatest() - got: " + resp.body)
        val source = extractHitSource(resp.body)
        source match {
          case Some(source) => Ok(source)
          case None => NotFound("Super hero not found !")
        }
      }
    )
  }

  def readThread(threadId: Int) = Action.async { request =>
    // Should be faster because does not compute scoring.
    // (see http://www.elasticsearch.org/guide/en/elasticsearch/guide/current/_finding_exact_values.html)
    val rep = RestClient.search {
      search in "chitchat"->"chitchat" query { matchall } filter { termFilter("thread", threadId) } sort {
        by field "createdAt" order(SortOrder.ASC)
      }
    }
    rep.map(
      resp => {
        Logger.debug("readThread() - got: " + resp.body)
        val sources: JsArray = extractHitsSources(resp.body)
        Ok(sources)
      }
    )}

  def searchChitchat(q: String, l: Int) = Action.async { request =>
    val rep = RestClient.search { search in "chitchat"->"chitchat" query { term("text", q) } limit l }
    rep.map(
      resp => {
        Logger.debug("searchChitchat() - got: " + resp.body)
        val sources: JsArray = extractHitsSources(resp.body)
        Ok(sources)
      }
    )
  }

  /**
   * Extract hits/hits from ES Json response
   * @param response ES Json response as String
   * @return A JsArray containing hits
   */
  def extractHits(response: String): JsArray = {
    return Json.parse(response).transform(hitsExtractor).get
  }

  /**
   * Extract hits/hits/_source from ES Json response
   * @param response ES Json response as String
   * @return A JsArray containing sources
   */
  def extractHitsSources(response: String): JsArray = {
    val hits: JsArray = extractHits(response)
    return JsArray(hits.value.map(_.transform(sourceExtractor).get))
  }

  /**
   * Extract hits/hits/_source from ES Json response in case of only one result expected
   * @param response ES Json response as String
   * @return An Option[JSValue] or None if no result
   */
  def extractHitSource(response: String): Option[JsValue] = {
    val hits: JsArray = extractHits(response)
    if (!hits.value.isEmpty) {
      return Option(hits.value(0).transform(sourceExtractor).get)
    }
    None
  }
}
