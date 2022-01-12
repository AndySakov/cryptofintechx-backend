package utils.json

import play.api.data.Form
import play.api.libs.json.Json
import play.api.libs.json._
import models.User
import play.api.data.FormError
import Message._
import models.requests.Request
import play.api.data.format.Formatter
import models.Categories.Category

object Writes {
  implicit object FormErrorWrites extends Writes[FormError] {
    override def writes(o: FormError): JsValue = Json.obj(
      "field" -> Json.toJson(o.key),
      "errorCode" -> Json.toJson(o.message),
    )
  }

  implicit object UserFormWithErrorsWrites extends Writes[Form[User]] {
    override def writes(formWithErrors: Form[User]): JsValue = Json.obj(
      "error" -> Json.toJson(true),
      "message" -> JsString("Error processing request"),
      "reason" -> JsString("Bad or missing data in form"),
      "fields" -> Json.toJson(formWithErrors.errors),
    )
  }

  implicit object RequestFormWithErrorsWrites extends Writes[Form[Request]] {
      override def writes(formWithErrors: Form[Request]): JsValue = Json.obj(
        "error" -> Json.toJson(true),
        "message" -> JsString("Error processing request"),
        "reason" -> JsString("Bad or missing data in form"),
        "fields" -> Json.toJson(formWithErrors.errors),
      )

      def write[A <: Request](formWithErrors: Form[A]): JsValue = Json.obj(
              "error" -> Json.toJson(true),
              "message" -> JsString("Error processing request"),
              "reason" -> JsString("Bad or missing data in form"),
              "fields" -> Json.toJson(formWithErrors.errors),
            )
    }

  implicit object UserWrites extends Writes[User] {
    override def writes(u: User): JsValue = Json.obj(
      "user" -> Json.obj(
        "user_id" -> Json.toJson(u.userID),
        "email" -> Json.toJson(u.email),
        "full_name" -> Json.toJson(u.fullName),
        "country" -> Json.toJson(u.country),
        "avatar_url" -> Json.toJson(u.avatarURL),
        "phone_number" -> Json.toJson(u.phoneNumber),
        "category" -> Json.toJson(u.category),
        "dob" -> Json.toJson(u.dob),
        "created_at" -> Json.toJson(u.createdAt)
      )
    )
  }

  implicit object SuccessWrites extends Writes[Success] {
    override def writes(s: Success): JsValue = Json.obj(
      "success" -> Json.toJson(true),
      "message" -> Json.toJson(s.message),
    )
  }

  implicit object ErrorWrites extends Writes[Error] {
    override def writes(s: Error): JsValue = Json.obj(
      "error" -> Json.toJson(true),
      "message" -> Json.toJson(s.message),
    )
  }

  implicit object FailureWrites extends Writes[Failure] {
    override def writes(s: Failure): JsValue = Json.obj(
      "success" -> Json.toJson(false),
      "message" -> Json.toJson(s.message),
    )
  }

  implicit object ServerErrorWrites extends Writes[Throwable] {
    override def writes(s: Throwable): JsValue = Json.obj(
      "error" -> Json.toJson(true),
      "errCode" -> Json.toJson(s.getClass.getName.toLowerCase),
      "message" -> Json.toJson(s.getMessage),
    )
  }
}
