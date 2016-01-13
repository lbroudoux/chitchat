import play.api._

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mapping.FieldType._
/**
 * @author laurent
 */
object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
    /*
    val client = ElasticClient.remote("localhost", 9300)
    client.execute {
      create index "chitchat" mappings (
        "chitchat" as (
          "author" typed StringType index "not_analyzed",
          "thread" typed LongType
        )
      )
    }
    */
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }
}
