import spray.routing._
import CouchMethods.Couch.getDoc
import CouchMethods.Couch.init
import CustomDirectives.CustomDirectives.time

object Main extends App with SimpleRoutingApp {
  implicit val system = init("logs")
  implicit val executionContext = system.dispatcher

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
          getDoc("logs", "_design/ave_time/_view/routes")
        }
      }
    }
  }
}