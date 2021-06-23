package dao


import api.misc.Message
import models.User
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.sql.SQLIntegrityConstraintViolationException
import java.time.{LocalDate, LocalDateTime}
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
  def createUser(newbie: User): Future[(Boolean, String)] = db.run({
//    sql"""insert into users (name, email, country, ADDRESS, dob, pass, phone, TOC, UUID) values ('${newbie.name}', '${newbie.email}', '${newbie.country}', '${newbie.address}', '${newbie.dob.toString}', '${newbie.pass}', '${newbie.phone}', '${newbie.toc.toString}', '${newbie.unique_id}')""".as[Int].asTry
    (Users += newbie) asTry
  }) map {
    case Failure(exception) => exception match {
      case _: SQLIntegrityConstraintViolationException => (false, "DUPLICATE_USER")
      case v => (false, v.toString)
    }
    case Success(_) => (true, "SUCCESS")
  }

  /**
   * Function to update a detail in a user entry
   * @param oldie the old version of the user entry
   * @param part the detail to update
   * @param new_detail the new detail
   * @return a future with unit
   */
  def updateUser(oldie: Future[Seq[User]], part: String, new_detail: String): Future[Unit] = {
    val user = Await.result(oldie, 10 seconds).head
    val op: DBIOAction[Unit, NoStream, Effect.Write] = part match {
      case "username" => Users.filter(x => x.unique_id === user.unique_id).map(_.email).update(new_detail).map(_ => ())
      case "pass" => Users.filter(x => x.unique_id === user.unique_id).map(_.pass).update(new_detail).map(_ => ())
      case "name" => Users.filter(x => x.unique_id === user.unique_id).map(_.name).update(new_detail).map(_ => ())
    }
    db.run(op)
  }

  /**
   * Function to select a user entry in the database
   * @param email the username of the user to select
   * @param pass the password of the user to select
   * @return a future with a sequence containing the user if it exists
   */
  def getUser(email: String, pass: String): Future[Seq[User]] = {
    db.run[Seq[User]](Users.filter(v => v.email === email && v.pass === pass).result)
  }

  /**
   * Function to delete a user entry from the database
   * @param email the username of the user to delete
   * @param pass the password of the user to delete
   * @return a future with unit
   */
  def deleteUser(email: String, pass: String): Future[Unit] = {
    db.run(Users.filter(x => x.email === email && x.pass === pass).delete).map(_ => ())
  }


  private class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def unique_id: Rep[String] = column[String]("UUID", O.Unique)
    def email: Rep[String] = column[String]("email", O.Unique, O.PrimaryKey)
    def country: Rep[String] = column[String]("country")
    def name: Rep[String] = column[String]("name")
    def dob: Rep[LocalDate] = column[LocalDate]("dob")
    def phone: Rep[String] = column[String]("phone")
    def pass: Rep[String] = column[String]("pass")
    def toc: Rep[LocalDateTime] = column[LocalDateTime]("TOC")
    def address: Rep[String] = column[String]("address")

    def * = (unique_id, email, address, country, name, dob, phone, pass, toc) <> (User.tupled, User.unapply)
  }
}