package controllers

import api.misc.exceptions.UserNotFoundAtLoginException
import api.misc.Category
import api.utils.Generator.randomID
import api.utils.Utils
import api.utils.Utils._
import courier.Defaults._
import dao._
import models._
import play.api.libs.json.Json
import play.api.mvc._
import views.html.helper.CSRF

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}
import javax.inject._
import scala.concurrent.Future
import scala.concurrent.Await

/**
* This controller creates an `Action` to handle HTTP requests to the
* application's home page.
*/
@Singleton
class HomeController @Inject()(users: UserDAO, sessions: SessionDAO, val controllerComponents: ControllerComponents) extends BaseController {
  
  // UI RENDERING
  /**
  * Error 404 custom return handler
  *
  * The configuration in the `routes` file means that this method
  * will be called when the application receives a `GET` request with
  * a path of `/` or any other undefined path.
  */
  
  def validateRequest[A](action: User => Result)(implicit request: Request[A]): Future[Result] = {
    val user = request.session.get("user_id").getOrElse("null")
    val token = request.session.get("token").getOrElse("null")
    sessions.validateSession(user, Token(token)) match {
      case true => Future.successful(
        Await.result(users.getUser(user), 5 seconds) match {
          case Left(_) => throw UserNotFoundAtLoginException("You might need to login again as your user no longer exists.")
          case Right(user) => action(user)
        }
      )
      case false => Future.successful(
      sessionExpired(request.path)
      )
    }
  }
  def notFound(O: String): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    NotFound(views.html.err404())
  }
  
  def sessionExpired(redirect: String): Result = 
  Unauthorized(views.html.err401(redirect))
  
  def index(): Action[AnyContent] = Action{
    implicit request: Request[AnyContent] =>
    Ok(views.html.index(CSRF.formField))
  }
  
  def about(): Action[AnyContent] = Action{
    implicit request: Request[AnyContent] =>
    Ok(views.html.about())
  }
  
  def todo(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
    Ok(views.html.todo())
  }
  
  def logout(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
    val token = request.session.get("token").get
    sessions.deleteSession(Token(token)).map(_ => 
    Redirect("/").withNewSession
    )
  }
  
  def dashboard(path: String): Action[AnyContent] = Action.async { 
    implicit request: Request[AnyContent] =>
    validateRequest{
      user =>
        path match {
          case x if x equals "" => Ok(views.html.dashboard(user))
        }
    }
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
        val phone = data("phone").head
        val country = data("country").head
        val category = Category.withName(data("category").head)
        users.createUser(User(
        user_id = randomID,
        email = email,
        country = country,
        name = name,
        dob = dob,
        phone = phone,
        pass = pass,
        createdAt = Timestamp.from(Instant.now()),
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
          case Right(user) => {
            val token = sessions.newSession(user.user_id)
            Redirect("/dashboard").withSession("token" -> token.token, "user_id" -> user.user_id)
          }
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
}
