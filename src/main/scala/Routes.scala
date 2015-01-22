import spray.routing.SimpleRoutingApp
import akka.actor.Actor._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import spray.routing._
import Directives._
import spray.http.HttpRequest
import spray.http.HttpResponse

object Main extends App with SimpleRoutingApp {
  implicit val system = ActorSystem()

  def log(timeInterval: Long): Unit = {
  }
  
  def time(): Directive0 = {
    mapRequestContext { ctx => 
      val timeStamp = System.currentTimeMillis
      ctx.withHttpResponseEntityMapped { response =>
        val totalTime = System.currentTimeMillis - timeStamp
        (ctx.request, response, System.currentTimeMillis - timeStamp)
        log(totalTime)
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
    }
  }
}