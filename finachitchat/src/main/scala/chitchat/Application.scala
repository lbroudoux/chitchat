package chitchat

import scala.util.{Success, Failure}
import scala.concurrent.{Future => ScalaFuture, Promise => ScalaPromise}
import scala.concurrent.ExecutionContext.Implicits.global
import com.twitter.util.{Future, Promise}
import com.twitter.finatra._
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import org.boon.json.JsonFactory
import org.elasticsearch.search.sort.SortOrder

/**
 * @author laurent
 */
object Application extends FinatraServer {

  class ChitchatApp extends Controller {

    val boonMapper =  JsonFactory.create()

    val esClient = ElasticClient.remote("localhost", 9300)

    post("/chitchat") { request  =>
      log.debug("post() - got: " + request.contentString)
      val chitchatM: ChitChat = boonMapper.fromJson(request.contentString, classOf[ChitChat])
      chitchatM.setCreatedAt(System.currentTimeMillis())
      esClient.execute {
        index into "chitchat/chitchat" fields (
          "author" -> chitchatM.getAuthor,
          "text" -> chitchatM.getText,
          "thread" -> chitchatM.getThread,
          "createdAt" -> chitchatM.getCreatedAt
          )
      }
      render.status(201).toFuture
    }

    get("/chitchat/latest/:superhero") { request =>
      request.routeParams.get("superhero") match {
        case Some(superhero) => {
          fromScala(
            esClient.execute {
              search in "chitchat" -> "chitchat" query { matchall } filter { termFilter ("author", superhero) } sort {
                by field "createdAt" order (SortOrder.DESC)
              }
            }.map(
              resp => {
                val source = resp.getHits.getAt(0).sourceAsString()
                //render.contentType("application/json").body(source)
                render.header("Content-Type", "application/json").body(source)
              }
            )
          )
        }
        case None => render.plain("superhero part needed").status(401).toFuture
      }
    }

    get("/chitchat/thread/:threadId") { request =>
      request.routeParams.get("threadId") match {
        case Some(threadId) => {
          fromScala(
            esClient.execute {
              search in "chitchat" -> "chitchat" query { matchall } filter { termFilter("thread", threadId) } sort {
                by field "createdAt" order (SortOrder.ASC)
              }
            }.map(
              resp => {
                val sources: StringBuilder = new StringBuilder("[")
                resp.getHits.getHits.foreach( hit =>
                  sources.append(hit.getSourceAsString).append(",")
                )
                //render.contentType("application/json").body(sources.deleteCharAt(sources.length - 1).append("]").toString())
                render.header("Content-Type", "application/json").body(sources.deleteCharAt(sources.length - 1).append("]").toString())
              }
            )
          )
        }
        case None => render.plain("threadId part needed").status(401).toFuture
      }
    }

    get("/chitchat/search") { request =>
      request.params.get("q") match {
        case Some(q) => render.ok.toFuture
        case None => render.plain("q param needed").status(401).toFuture
      }
    }

    def fromScala[A](scalaFuture: ScalaFuture[A]): Future[A] = {
      val promise = Promise[A]()
      scalaFuture onComplete {
        case Success(a) => promise setValue a
        case Failure(e)  => promise raise e
      }
      promise
    }
  }

  register(new ChitchatApp())
}
