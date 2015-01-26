package UnmarshallHelpers

import play.api.libs.json._
import play.api.libs.functional.syntax._
import spray.httpx.unmarshalling._
import spray.http._
import spray.httpx.marshalling.Marshaller
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

  implicit val intMarshaller = Marshaller.of[Int](`application/json`) {
    (value, ct, ctx) => ctx.marshalTo(HttpEntity(ct, s"""{ "average": $value }"""))
  }
  implicit val reduceDocsMarshaller = Marshaller.of[List[ReduceDoc]](`application/json`) {
    (value, ct, ctx) => ctx.marshalTo(HttpEntity(ct, s"""${Json.toJson(value)}"""))
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