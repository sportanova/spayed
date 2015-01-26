package CouchMethods

import scala.concurrent.Future
import UnmarshallHelpers._
import UnmarshallHelpers.ReduceDoc
import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.http.ContentTypes._
import spray.http.HttpMethods._
import spray.httpx.SprayJsonSupport._
import spray.json._

object Couch {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  case class RouteTime(route: String, time: Long, `type`: String)

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val docFormat = jsonFormat3(RouteTime)
  }
  import MyJsonProtocol._
  
  def createDB(dbName: String): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    pipeline(Put(s"http://127.0.0.1:5984/$dbName"))
  }

  def getDoc(dbName: String, doc: String): Future[List[ReduceDoc]] = {
    val pipeline: HttpRequest => Future[List[ReduceDoc]] = (
      sendReceive
      ~> unmarshal[List[ReduceDoc]]
    )

    val req = HttpRequest(method = GET, uri = s"http://127.0.0.1:5984/$dbName/$doc")
    pipeline(req)
  }

  def insertDoc(dbName: String, doc: RouteTime): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = (sendReceive)

    pipeline(Post(s"http://127.0.0.1:5984/$dbName", doc))
  }

  def insertView(dbName: String, viewPath: String, view: String): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = (
      sendReceive
    )

    val req = HttpRequest(method = PUT, uri = s"http://127.0.0.1:5984/$dbName$viewPath", entity = HttpEntity(`application/json`, viewJSON))
    pipeline(req)
  }

  val viewJSON = """ {"language":"javascript","views":{"routes":{"reduce": "function (keys, values, rereduce) {if(rereduce) {var sum = 0;var count = 0;for(var i = 0; i < values.length; i++) {sum = sum + values[i][0];count = count + values[i][1];}return [sum, count];} else {var sum = 0;var count = 0;for(var i = 0; i < values.length; i++) {count++;sum = sum + values[i];}return [sum, count];}}","map":"function(doc) { if (doc.type == 'routeTime')  emit(doc.route, doc.time) }"}}} """
    
  def init(dbName: String): ActorSystem = {
    createDB(dbName)
    insertView(dbName, "/_design/ave_time", viewJSON)
    
    system
  }
}