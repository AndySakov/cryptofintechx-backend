package models.requests

import models.UserProfile

sealed trait Request

final case class ProfileUpdateRequest(newProfile: UserProfile) extends Request
final case class PasswordChangeRequest(oldPassword: String, newPassword: String) extends Request
final case class AuthRequest(email: String, password: String) extends Request
final case class ValidatePinRequest(transactionPin: Int) extends Request