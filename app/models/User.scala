package models

import java.sql.Timestamp
import java.time.LocalDate
import api.misc.CategoryImpl.Category

/** This is the User model that defines the user structure in the database
  * @param user_id
  *   a unique ID that all users possess
  * @param email
  *   the email associated with this user
  * @param pass
  *   the password for this user account
  * @param name
  *   the full name of this user
  * @param dob
  *   the date of birth of the user
  * @param phone
  *   the phone number of the user
  * @param category
  *   the user account class
  * @param country
  *   the country of origin
  * @param createdAt
  *   the time of creation of this user account accurate to milliseconds
  */
final case class User(
  id: Long = 0L,
  user_id: String,
  email: String,
  country: String,
  name: String,
  dob: LocalDate,
  phone: String,
  pass: String,
  category: Category,
  createdAt: Timestamp
) extends Serializable
