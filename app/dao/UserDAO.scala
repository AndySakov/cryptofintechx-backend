package dao


import api.misc.Message
import api.misc.exceptions._
import api.utils.BCrypt._
import models.User
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.{SQLIntegrityConstraintViolationException, Timestamp}
import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class UserDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) {

  type Cr8Result = (Boolean, Message.Value)
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import dbConfig.profile.api._

  private val Users = TableQuery[UsersTable]

  /**
   * Function to create a new user
   * @param newbie the user to create
   * @return a future with the result of the operation
   */
  def createUser(newbie: User): Future[(Boolean, String)] = {
    db.run({
      (Users += newbie.copy(pass = hashpw(newbie.pass).getOrElse(throw PasswordNotHashableException("Password could not be hashed!")))) asTry
    }) map {
      case Failure(exception) => exception match {
        case _: SQLIntegrityConstraintViolationException => throw EmailTakenException("An account already exists for this email")
        case v => throw UserCreateFailedException(v.getMessage)
      }
      case Success(_) => (true, "SUCCESS")
    }
  }

  /**
   * Function to update a detail in a user entry
   * @param part the detail to update
   * @param new_detail the new detail
   * @return a future with unit
   */
  def updateUser(email: String, pass: String, part: String, new_detail: String): Future[Unit] = {
    Await.result(getUser(email, pass), 10 seconds) match {
      case Left(_) => throw UserUpdateFailedException("Could not update user because user not found!")
      case Right(user) =>
        val op: DBIOAction[Unit, NoStream, Effect.Write] = part match {
          case "email" => Users.filter(x => x.unique_id === user.unique_id).map(_.email).update(new_detail).map(_ => ())
          case "password" => Users.filter(x => x.unique_id === user.unique_id).map(_.pass).update(hashpw(new_detail).getOrElse(throw PasswordNotHashableException("Password could not be hashed!"))).map(_ => ())
          case "name" => Users.filter(x => x.unique_id === user.unique_id).map(_.name).update(new_detail).map(_ => ())
          case "phone" => Users.filter(x => x.unique_id === user.unique_id).map(_.phone).update(new_detail).map(_ => ())
        }
        db.run(op)
    }
  }

  /**
   * Function to select a user entry in the database
   * @param email the username of the user to select
   * @param pass the password of the user to select
   * @return a future with a sequence containing the user if it exists
   */
  def getUser(email: String, pass: String): Future[Either[Boolean, User]] = {
    db.run(Users.filter(v => v.email === email).result) map {
      case result: Seq[UsersTable#TableElementType] => checkpw(pass, result.head.pass) match {
        case Right(_) => Right(result.head)
        case Left(_) => Left(false)
      }
      case _ => Left(false)
    }
  }

  /**
   * Function to select a user entry in the database
   * @param userID the unique id of the user to select
   * @return a future containing either a boolean or a user
   */
  def getUser(userID: String): Future[Either[Boolean, User]] = {
    db.run(Users.filter(v => v.unique_id === userID).result) map {
      case result: Seq[UsersTable#TableElementType] => if(result.isEmpty){
        Left(false)
      } else {
        Right(result.head)
      }
      case _ => Left(false)
    }
  }

  /**
   * Function to delete a user entry from the database
   * @param email the username of the user to delete
   * @param pass the password of the user to delete
   * @return a future with unit
   */
  def deleteUser(email: String, pass: String): Future[Future[Either[Boolean, Boolean]]] = {
    getUser(email, pass) map {
      case Right(user) =>
        db.run{
          Users.filter(_.email === user.email).delete
        } map {
          case 1 => Right(true)
          case _ => Left(false)
        }
      case Left(_) => throw UserDeleteFailedException("")
    }
  }


  private class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def unique_id: Rep[String] = column[String]("UUID", O.PrimaryKey)
    def email: Rep[String] = column[String]("email", O.Unique, O.PrimaryKey)
    def country: Rep[String] = column[String]("country")
    def name: Rep[String] = column[String]("name")
    def dob: Rep[LocalDate] = column[LocalDate]("dob")
    def phone: Rep[String] = column[String]("phone")
    def pass: Rep[String] = column[String]("pass")
    def toc: Rep[Timestamp] = column[Timestamp]("TOC")
    def category: Rep[String] = column[String]("category")

    def * = (unique_id, email, country, name, dob, phone, pass, toc, category) <> (User.tupled, User.unapply)
  }
}