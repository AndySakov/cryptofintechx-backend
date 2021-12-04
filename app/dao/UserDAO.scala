package dao

import api.misc.CategoryImpl._
import api.misc.Message._
import api.misc._
import api.utils.BCrypt._
import api.utils.Utils.exec
import models.{User, UserProfile}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.{SQLIntegrityConstraintViolationException, Timestamp}
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class UserDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  //noinspection TypeAnnotation
  implicit val categoryMapper = MappedColumnType.base[Category, String](_.toString, CategoryImpl.withName)

  val Users = TableQuery[UsersTable]

  /**
   * Function to create a new user
   * @param newbie the user to create
   * @return a future with the result of the operation
   */
  def createUser(newbie: User): ResultSet[User] = {
    Try(
      exec(db.run(Users += newbie.copy(pass = encrypt(newbie.pass))))
    ) match {
      // TODO: Add catch for duplicate users with IntegrityException blah blah blah
      case Failure(exception) =>
        val message: Message = exception match {
          case _: SQLIntegrityConstraintViolationException =>
            DuplicateUserEntry
          case _ => UnknownFailure
        }
        ResultSet(ResultTypeImpl.FAILURE, Result(message, None))
      case Success(_) => ResultSet(ResultTypeImpl.SUCCESS, Result(UserCreateSuccessful, Some(newbie)))
    }
  }

  /**
   * Function to update a users profile
   * @param user_id the unique id of the user to update
   * @param updated_profile the new detail
   * @return a future with unit
   */
  def updateUserProfile(user_id: String, updated_profile: UserProfile): ResultSet[User] = {
    getUser(user_id) match {
      case None => ResultSet(ResultTypeImpl.FAILURE, Result(UserNotFound, None))
      case Some(user) =>
        val query = Users.filter(x => x.user_id === user.user_id).map(
          user => (user.email, user.name, user.phone).mapTo[UserProfile]
        ).update(updated_profile)
        val result = exec(db.run(query andThen Users.filter(_.user_id === user.user_id).result)).headOption
        ResultSet(ResultTypeImpl.SUCCESS, Result(UserUpdateSuccessful, result))
    }
  }

  /**
   * Function to select a user entry in the database
   * @param email the username of the user to select
   * @param pass the password of the user to select
   * @return a future with a sequence containing the user if it exists
   */
  def getUser(email: String, pass: String): ResultSet[_ <: User] = {
    exec(db.run(Users.filter(v => v.email === email).result) map {
      result =>
        if(result.nonEmpty) {
          if (validate(pass, result.head.pass)) {
            ResultSet(ResultTypeImpl.SUCCESS, Result(UserAuthSuccessful, result.headOption))
          } else {
            ResultSet(ResultTypeImpl.FAILURE, Result(InvalidCredentials, None))
          }
        } else {
          ResultSet(ResultTypeImpl.FAILURE, Result(UserNotFound, None))
        }
    })
  }

  /**
   * Function to select a user entry in the database
   * @param user_id the unique id of the user to select
   * @return a future containing either a boolean or a user
   */
  private def getUser(user_id: String): Option[User] = {
    exec(db.run(Users.filter(v => v.user_id === user_id).result) map {
      case result: Seq[UsersTable#TableElementType] => result.headOption
      case _ => None
    })
  }

  /**
   * Function to delete a user entry from the database
   * @param user_id the unique id of the user to delete
   * @return a future with unit
   */
  def deleteUser(user_id: String): ResultSet[Boolean] = {
    getUser(user_id) match {
      case Some(user) =>
        exec(db.run{
          Users.filter(_.user_id === user.user_id).delete
        } map {
          case 1 => ResultSet(ResultTypeImpl.SUCCESS, Result(UserAuthSuccessful, Some(true)))
          case _ => ResultSet(ResultTypeImpl.ERROR, Result(UnknownError, Some(false)))
        })
      case None =>
        ResultSet(ResultTypeImpl.FAILURE, Result(UserNotFound, None))
    }
  }


  class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def user_id: Rep[String] = column[String]("user_id", O.Unique)
    def email: Rep[String] = column[String]("email", O.Unique)
    def country: Rep[String] = column[String]("country")
    def name: Rep[String] = column[String]("name")
    def dob: Rep[LocalDate] = column[LocalDate]("dob")
    def phone: Rep[String] = column[String]("phone")
    def pass: Rep[String] = column[String]("pass")
    def category: Rep[Category] = column[Category]("category")
    def createdAt: Rep[Timestamp] = column[Timestamp]("createdAt")

    def * = (id, user_id, email, country, name, dob, phone, pass, category, createdAt).mapTo[User]
  }
}