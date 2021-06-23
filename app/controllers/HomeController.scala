package controllers

import api.utils.UUIDGenerator.randomUUID
import dao.UserDAO
import models.User
import play.api.libs.json.Json
import play.api.mvc._
import views.html.helper.CSRF

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(users: UserDAO, val controllerComponents: ControllerComponents) extends BaseController {

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

  /**
   * A function to extract the body of a request sent as xxx-form-url-encoded
   * @param request the request to extract the body from
   * @return the extracted body
   */
  def body(implicit request: Request[AnyContent]): Option[Map[String, Seq[String]]] = {
    request.body.asFormUrlEncoded
  }

  def index(): Action[AnyContent] = Action{
    implicit request: Request[AnyContent] =>
      Ok(views.html.index(CSRF.formField))
  }

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
          users.createUser(User(
            unique_id = randomUUID,
            email = email,
            address = address,
            country = country,
            name = name,
            dob = dob,
            phone = phone,
            pass = pass,
            toc = LocalDateTime.now())
          ).map(x => Ok(x._2.toString))
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
          users.updateUser(users.getUser(email, pass), part, update).map(_ => Ok(Json.obj(("success", Json.toJson(true)))))
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
          users.getUser(email, pass).map(v => Ok("SUCCESS"))
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
          users.deleteUser(email, pass).map(_ => Ok(Json.obj(("success", Json.toJson(true)))))
        case None => Future(Forbidden(Json.obj(("error", Json.toJson("Request contained no data!")))))
      }
    }
  }
}
 