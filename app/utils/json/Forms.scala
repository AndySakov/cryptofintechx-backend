package utils.json

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import java.util.UUID
import play.api.data.validation.Constraints.pattern
import java.sql.Timestamp
import models.User
import models.requests._
import java.time.Instant
import models.UserProfile
import models.Categories.Category
import utils.json.Formats._

object Forms {
  private def phoneNumber =
    nonEmptyText.verifying(pattern("""[0-9.+]+""".r, error = "A valid phone number is required"))

  val createUserForm: Form[User] = Form(
    mapping(
      "id" -> optional(of[Long]),
      "user_id" -> ignored(UUID.randomUUID),
      "email" -> email,
      "country" -> nonEmptyText,
      "full_name" -> nonEmptyText,
      "phone" -> optional(phoneNumber),
      "password" -> nonEmptyText(minLength = 8, maxLength = 30),
      "avatar_url" -> optional(text),
      "category" -> of[Category],
      "dob" -> localDate("yyyy-MM-dd"),
      "created_at" -> ignored(Timestamp.from(Instant.now())),
    )(User.apply)(User.unapply)
  )

  val authUserForm: Form[AuthRequest] = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText(minLength = 8, maxLength = 30),
    )(AuthRequest.apply)(AuthRequest.unapply)
  )

  val changePasswordForm: Form[PasswordChangeRequest] = Form(
    mapping(
      "old_password" -> nonEmptyText(minLength = 8, maxLength = 30),
      "new_password" -> nonEmptyText(minLength = 8, maxLength = 30),
    )(PasswordChangeRequest.apply)(PasswordChangeRequest.unapply)
  )

  val updateProfileForm: Form[ProfileUpdateRequest] = Form(
    mapping(
      "new_profile" -> mapping(
        "phone_number" -> optional(phoneNumber),
        "avatar_url" -> optional(text)
      )(UserProfile.apply)(UserProfile.unapply)
    )(ProfileUpdateRequest.apply)(ProfileUpdateRequest.unapply)
  )

}
