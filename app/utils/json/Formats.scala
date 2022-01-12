package utils.json

import java.time.format.DateTimeFormatter
import play.api.libs.json.Format
import java.time.LocalDateTime
import play.api.libs.json._
import java.sql.Timestamp
import models.User
import utils.Helpers._
import models.Categories
import models.Categories.Category
import play.api.data.format.Formatter

object Formats {
  implicit object timestampFormat extends Format[Timestamp] {
    val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    def reads(json: JsValue) = {
      val str = json.as[String]
      val ldt = LocalDateTime.parse(str, format)
      JsSuccess(Timestamp.valueOf(ldt))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts.toInstant()))
  }

  implicit val categoryFormatter: Formatter[Category] = enumerationFormatter(Categories)
}
