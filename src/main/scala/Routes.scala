import spray.routing._
import Directives._
import CouchMethods.Couch
import UnmarshallHelpers.UnmarshallHelpers.ReduceDoc

object Main extends App with SimpleRoutingApp {
  implicit val system = Couch.init("logs")
  implicit val executionContext = system.dispatcher

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