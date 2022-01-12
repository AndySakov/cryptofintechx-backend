package models

final case class UserProfile(
    phoneNumber: Option[String],
    avatarURL: Option[String],
  ) extends Serializable
