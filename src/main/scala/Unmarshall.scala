package UnmarshallHelpers

import play.api.libs.json._
import play.api.libs.functional.syntax._
import spray.httpx.unmarshalling._
import spray.http._
import spray.httpx.marshalling.Marshaller
import ContentTypes._
import scala.util.{Try, Success, Failure}

object UnmarshallHelpers {
  case class ReduceDoc(key: Option[String], value: List[Int])
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

  implicit val ReduceDocReads = Json.reads[ReduceDoc]

  implicit val reduceDocWrites = new Writes[ReduceDoc] {
    def writes(r: ReduceDoc): play.api.libs.json.JsValue = {
      Json.obj(
        "key" -> r.key,
        "value" -> r.value
      )
    }
  }

  def calcAverageToTenth(n: Float, d: Float): BigDecimal = {
    BigDecimal(n.toDouble / d.toDouble).setScale(1, BigDecimal.RoundingMode.HALF_UP) 
  }

  def getAverage(docs: List[ReduceDoc]): Option[BigDecimal] = {
    docs.headOption match {
      case Some(doc) => {
        for {
          sum <- doc.value.lift(0)
          count <- doc.value.lift(1)
        } yield (calcAverageToTenth(sum, count))
      }
      case None => None
    }
  }

  implicit val intMarshaller = Marshaller.of[Int](`application/json`) {
    (value, ct, ctx) => ctx.marshalTo(HttpEntity(ct, s"""{ "average": $value }"""))
  }
  implicit val reduceDocsMarshaller = Marshaller.of[List[ReduceDoc]](`application/json`) {
    (value, ct, ctx) => ctx.marshalTo(HttpEntity(ct, s"""${Json.toJson(getAverage(value))}"""))
  }
}