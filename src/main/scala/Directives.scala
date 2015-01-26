package CustomDirectives

import spray.routing._
import Directives._
import CouchMethods.Couch.insertDoc
import CouchMethods.Couch.RouteTime

object CustomDirectives {
  def time(): Directive0 = {
    mapRequestContext { ctx => 
      val timeStamp = System.currentTimeMillis
      ctx.withHttpResponseEntityMapped { response =>
        val totalTime = System.currentTimeMillis - timeStamp
        insertDoc("logs", RouteTime("/hello", totalTime, "routeTime"))
        response
      }
    }
  }
}