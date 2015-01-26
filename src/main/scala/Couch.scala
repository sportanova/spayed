package CouchMethods

import spray.routing.SimpleRoutingApp
import akka.actor.Actor._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import spray.routing._
import Directives._
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.http._
import HttpMethods._
import HttpHeaders._
import ContentTypes._
import spray.http._
import spray.httpx.marshalling.Marshaller
import spray.client.pipelining._
import scala.concurrent.Future
import spray.httpx.SprayJsonSupport._
import spray.json._
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext
import spray.httpx.PlayJsonSupport
import play.api.libs.json._
import play.api.libs.functional.syntax._
import spray.httpx.unmarshalling._
import spray.httpx.unmarshalling._
import spray.util._
import spray.http._
import ContentTypes._
import UnmarshallHelpers._
import UnmarshallHelpers.ReduceDoc

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
      addHeader("", "")
      ~> sendReceive
      ~> unmarshal[List[ReduceDoc]]
    )

    val req = HttpRequest(method = GET, uri = s"http://127.0.0.1:5984/$dbName/$doc")
    pipeline(req)
  }

  def insertDoc(dbName: String, doc: RouteTime): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = (
      addHeader("", "")
      ~> sendReceive
    )

    pipeline(Post(s"http://127.0.0.1:5984/$dbName", doc))
  }

  def insertView(dbName: String, viewPath: String, view: String): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = (
      addHeader("", "")
      ~> sendReceive
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