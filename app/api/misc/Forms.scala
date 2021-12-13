package api.misc

import api.misc.CategoryImpl.Category
import api.misc.CustomWrites.categoryFormatter
import api.utils.Generator.randomID
import models.{User, UserProfile}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.pattern

import java.sql.Timestamp
import java.time.Instant

object Forms {

  private def phoneNumber =
    nonEmptyText.verifying(pattern("""[0-9.+]+""".r, error = "A valid phone number is required"))
  val createUserForm: Form[User] = Form(
    mapping(
      "id"        -> ignored(0L),
      "user_id"   -> ignored(randomID),
      "email"     -> email,
      "country"   -> nonEmptyText,
      "name"      -> nonEmptyText,
      "dob"       -> localDate("yyyy-MM-dd"),
      "phone"     -> phoneNumber,
      "pass"      -> nonEmptyText(minLength = 8, maxLength = 32),
      "category"  -> of[Category],
      "createdAt" -> ignored(Timestamp.from(Instant.now()))
    )(User.apply)(User.unapply)
  )

  val updateUserForm: Form[UserProfile] = Form(
    mapping(
      "name"  -> nonEmptyText,
      "email" -> email,
      "phone" -> phoneNumber
    )(UserProfile.apply)(UserProfile.unapply)
  )

  val authUserForm: Form[(String, String)] = Form(
    tuple(
      "email" -> email,
      "pass"  -> nonEmptyText(minLength = 8, maxLength = 32)
    )
  )
}