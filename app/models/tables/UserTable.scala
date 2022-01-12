package models.tables

import models.User
import java.util.UUID
import java.time.LocalDate
import java.sql.Timestamp
import java.time.LocalDateTime
import models.Categories.Category
import models.Categories
import utils.Helpers.enumerationFormatter
import utils.pgsql.CustomPostgresProfile.api._


class UserTable(tag: Tag) extends Table[User](tag, Some("cryptofintechx"), "users") {

  implicit val categoryMapper =
            MappedColumnType.base[Category, String](_.toString, Categories.withName)

  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc, O.Unique)

  def userID = column[UUID]("user_id", O.Unique)

  def email = column[String]("email", O.Unique)

  def country = column[String]("country")

  def fullName = column[String]("full_name")

  def phoneNumber = column[Option[String]]("phone_number")

  def password = column[String]("password")

  def avatarURL = column[Option[String]]("avatar_url")

  def category = column[Category]("category")

  def dob = column[LocalDate]("dob")

  def createdAt = column[Timestamp]("created_at")

  def * = (id, userID, email, country, fullName, phoneNumber, password, avatarURL, category, dob, createdAt).mapTo[User]
}