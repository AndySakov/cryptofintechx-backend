package controllers

import api.misc.exceptions.UserNotFoundAtLoginException
import api.utils.UUIDGenerator.randomUUID
import api.utils.Utils
import api.utils.Utils.body
import courier.Defaults._
import dao.UserDAO
import models.User
import play.api.libs.json.Json
import play.api.mvc._
import views.html.helper.CSRF

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}
import javax.inject._
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(users: UserDAO, val controllerComponents: ControllerComponents) extends BaseController {

  // UI RENDERING
  /**
   * Error 404 custom return handler
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/` or any other undefined path.
   */
  def fof(O: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    O.toSeq
    NotFound(views.html.err404())
  }

  def index(): Action[AnyContent] = Action{
    implicit request: Request[AnyContent] =>
      Ok(views.html.index(CSRF.formField))
  }

  def forgot(): Action[AnyContent] = Action{
    implicit request: Request[AnyContent] =>
      Ok(views.html.forgot(CSRF.formField))
  }

  def todo(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(views.html.todo())
  }

  def logout(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      Future.successful(Redirect("/").withNewSession)
  }

  // CRUD STUFFS
  /**
   * User creation handler
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `POST` request with
   * a path of `/create/user`.
   */
  def createUser(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] => {
      body match {
        case Some(data) =>
          val email = data("email").head
          val pass = data("pass").head
          val name = data("name").head
          val dob = LocalDate.parse(data("dob").head, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
          val address = data("address").head
          val phone = data("phone").head
          val country = data("country").head
          val category = data("category").head
          users.createUser(User(
            unique_id = randomUUID,
            email = email,
            address = address,
            country = country,
            name = name,
            dob = dob,
            phone = phone,
            pass = pass,
            toc = Timestamp.from(Instant.now()),
            category = category)
          ).map(x => Redirect("/").flashing(Utils.flash("Welcome to CRYPTOFINTECHX.", "success"): _*))
        case None => Future(Forbidden(Json.obj(("error", Json.toJson("Request contained no data!")))))
      }
    }
  }

  /**
   * User update handler
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `PUT` request with
   * a path of `/update/user/:part` where part is the detail to update.
   */
  def updateUser(part: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] => {
      body match {
        case Some(data) =>
          val email = data("email").head
          val pass = data("pass").head
          val update = data("new_detail").head
          users.updateUser(email, pass, part, update).map(_ => Redirect("/").flashing(Utils.flash("User profile updated. Login again", "success"): _*))
        case None => Future(Forbidden(Json.obj(("error", Json.toJson("Request contained no data!")))))
      }
    }
  }

  /**
   * User authentication handler
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `POST` request with
   * a path of `/auth/user`.
   */
  def authUser(): Action[AnyContent] = Action.async{
    implicit request: Request[AnyContent] => {
      body match {
        case Some(data) =>
          val email = data("email").head
          val pass = data("pass").head
          users.getUser(email, pass).map {
            case Left(_) => throw UserNotFoundAtLoginException("Wrong login details!")
            case Right(user) => Ok(views.html.dashboard(CSRF.formField, user))
          }
        case None => Future(Forbidden(Json.obj(("error", Json.toJson("Request contained no data!")))))
      }
    }
  }

  /**
   * User deletion handler
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `DELETE` request with
   * a path of `/delete/user`.
   */
  def removeUser(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] => {
      body match {
        case Some(data) =>
          val email = data("email").head
          val pass = data("pass").head
          users.deleteUser(email, pass).map(_ => Redirect("/").flashing(Utils.flash("User deleted successfully!", "success"): _*))
        case None => Future(Forbidden(Json.obj(("error", Json.toJson("Request contained no data!")))))
      }
    }
  }

//  def reset(): Action[AnyContent] = Action.async{
//    implicit request: Request[AnyContent] =>
//      body match {
//        case Some(data) =>
//          val email = data("email").head
//          val mailer = Mailer("smtp.gmail.com", 587)
//            .auth(true)
//            .as(sys.env("MAILER_USER"), sys.env("MAILER_PASS"))
//            .startTls(true)()
//
//          mailer(Envelope.from(InternetAddress.parse(sys.env("MAILER_HOST")).head)
//            .to(InternetAddress.parse(email).head)
//            .subject("RESET YOUR PASSWORD")
//            .content(Text(s"""
//                             |<html>
//                             |  <body>
//                             |    <h1>Click the link below to reset your password!</h1>
//                             |    <a href="https://google.com">RESET PASSWORD</a>
//                             |  </body>
//                             |</html>""".stripMargin))).map{
//            _ =>
//              Redirect("/forgot").flashing(flash("Message Sent!", "success"): _*)
//          }
//        case None => Future.successful(Forbidden("No data gotten!"))
//
//      }
//  }
}
 