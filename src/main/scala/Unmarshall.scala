package UnmarshallHelpers

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

object UnmarshallHelpers {
  case class ReduceDoc(key: Option[String], value: List[Int])

  implicit val ReduceDocReads = Json.reads[ReduceDoc]

  implicit val reduceDocWrites = new Writes[ReduceDoc] {
    def writes(r: ReduceDoc): play.api.libs.json.JsValue = {
      Json.obj(
        "key" -> r.key,
        "value" -> r.value
      )
    }
  }

  object ReduceDoc {
    implicit val ReduceDocUnmarshaller = Unmarshaller[List[ReduceDoc]](MediaTypes.`text/plain`) {
      case HttpEntity.NonEmpty(contentType, data) => {
        (Json.parse(data.asString) \ "rows").validate[List[ReduceDoc]] match {
          case s: JsSuccess[List[ReduceDoc]] => s.get
          case e: JsError => List()
        }
      }
    }
  }
}