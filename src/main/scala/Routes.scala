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
import CouchMethods.Couch
import play.api.libs.json._

object Main extends App with SimpleRoutingApp {
  implicit val system = Couch.init("logs")
  implicit val executionContext = system.dispatcher
  implicit val intMarshaller = Marshaller.of[Int](`application/json`) {
    (value, ct, ctx) => ctx.marshalTo(HttpEntity(ct, s"""{ "average": $value }"""))
  }
  implicit val reduceDocsMarshaller = Marshaller.of[List[Couch.ReduceDoc]](`application/json`) {
    (value, ct, ctx) => ctx.marshalTo(HttpEntity(ct, s"""${Json.toJson(value)}"""))
  }

  def time(): Directive0 = {
    mapRequestContext { ctx => 
      val timeStamp = System.currentTimeMillis
      ctx.withHttpResponseEntityMapped { response =>
        val totalTime = System.currentTimeMillis - timeStamp
        Couch.insertDoc("logs", Couch.RouteTime("/hello", totalTime, "routeTime"))
        response
      }
    }
  }

  startServer(interface = "localhost", port = 8080) {
    time() {
      path("hello") {
        get {
          complete {
            <h1>Say hello to spray</h1>
          }
        }
      }
    } ~
    path("time") {
      get {
        complete {
          Couch.getDoc("logs", "_design/ave_time/_view/routes")
        }
      }
    }
  }
}