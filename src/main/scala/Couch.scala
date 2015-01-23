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
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext

object Couch {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  
  case class RouteTime(route: String, time: Long, `type`: String)
  
  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val docFormat = jsonFormat3(RouteTime)
  }
  import MyJsonProtocol._
  
  def createDB(dbName: String) = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val response: Future[HttpResponse] = pipeline(Put(s"http://127.0.0.1:5984/$dbName"))
  }

  def getAverage(json: String)(implicit ec: ExecutionContext): Int = {
    val aveTF = for {
      l <- Try{parse(json).values.asInstanceOf[Map[String,Any]]("rows").asInstanceOf[List[Map[String,List[BigInt]]]].head("value")}
      sum <- Try{l(0)}
      count <- Try{l(1)}
    } yield (sum / count)

    aveTF match {
      case Success(s) => s.toInt
      case Failure(ex) => 0
    }
  }

  def getDoc(dbName: String, doc: String): Future[String] = {
    val pipeline: HttpRequest => Future[String] = (
      addHeader("", "")
      ~> sendReceive
      ~> unmarshal[String]
    )
    pipeline(Get(s"http://127.0.0.1:5984/$dbName/$doc"))
  }

  def insertDoc(dbName: String, doc: RouteTime): Future[HttpResponse] = {
    val pipeline: HttpRequest => Future[HttpResponse] = (
      addHeader("", "")
      ~> sendReceive
    )

    pipeline(Post(s"http://127.0.0.1:5984/$dbName", doc)).map(x => {println(s"!!!!!! insert $x"); x})
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