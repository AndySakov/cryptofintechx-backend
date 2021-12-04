package controllers

import api.misc.CustomWrites._
import api.misc.Forms._
import api.misc.Message.{Error, Failure, Success, UnknownError}
import api.misc.Responses._
import api.misc.{Result, ResultSet, ResultTypeImpl}
import auth.{RequestAuthAction, SessionAuthAction}
import dao._
import models.Token
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
/**
* This controller creates an `Action` to handle HTTP requests to the
* application's home page.
*/
@Singleton
class HomeController @Inject()(users: UserDAO, sessions: SessionDAO, val controllerComponents: ControllerComponents, sessionAuthAction: SessionAuthAction, requestAuthAction: RequestAuthAction) extends BaseController {

  // CRUD STUFFS
  /**
  * User creation handler
  *
  * The configuration in the `routes` file means that this method
  * will be called when the application receives a `POST` request with
  * a path of `/create/user`.
  */
  def createUser(): Action[AnyContent] = requestAuthAction.async {
    implicit request: Request[AnyContent] => {
      createUserForm.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(UserFormWithErrorsWrites.writes(formWithErrors))),
        userData =>
          users.createUser(userData) match {
            case ResultSet(ResultTypeImpl.SUCCESS, result) =>
              result match {
                case Result(message, data) =>
                  Future.successful(Ok(successResponse(message.asInstanceOf[Success], data)))
              }
            case ResultSet(ResultTypeImpl.FAILURE, result) =>
              result match {
                case Result(message, _) =>
                  Future.successful(Ok(failedResponse(message.asInstanceOf[Failure])))
              }
            case ResultSet(ResultTypeImpl.ERROR, result) =>
              result match {
                case Result(message, _) =>
                  Future.successful(Ok(errorResponse(message.asInstanceOf[Error])))
              }
            case _ => Future.successful(BadRequest(errorResponse(UnknownError)))
          }
      )
    }
  }

  /**
  * User update handler
  *
  * The configuration in the `routes` file means that this method
  * will be called when the application receives a `PUT` request with
  * a path of `/update/user/:part` where part is the detail to update.
  */
  def updateUser(user_id: String): Action[AnyContent] = requestAuthAction.andThen(sessionAuthAction).async {
    implicit request =>
    updateUserForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(UserProfileFormWithErrorsWrites.writes(formWithErrors))),
      userProfile =>
        users.updateUserProfile(user_id, userProfile) match {
          case ResultSet(ResultTypeImpl.SUCCESS, result) =>
            result match {
              case Result(message, data) =>
                Future.successful(Ok(successResponse(message.asInstanceOf[Success], data)))
            }
          case ResultSet(ResultTypeImpl.FAILURE, result) =>
            result match {
              case Result(message, _) =>
                Future.successful(Ok(failedResponse(message.asInstanceOf[Failure])))
            }
          case ResultSet(ResultTypeImpl.ERROR, result) =>
            result match {
              case Result(message, _) =>
                Future.successful(Ok(errorResponse(message.asInstanceOf[Error])))
            }
          case _ => Future.successful(BadRequest(errorResponse(UnknownError)))
        }
    )
  }

  /**
  * User authentication handler
  *
  * The configuration in the `routes` file means that this method
  * will be called when the application receives a `POST` request with
  * a path of `/auth/user`.
  */
  def authUser(): Action[AnyContent] = requestAuthAction.async{
    implicit request: Request[AnyContent] => {
      authUserForm.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(AuthFormWithErrorsWrites.writes(formWithErrors))),
        credentials =>
          users.getUser(credentials._1, credentials._2) match {
            case ResultSet(ResultTypeImpl.SUCCESS, result) =>
              result match {
                case Result(message, data) =>
                  data.map(user => sessions.newSession(user.user_id))
                  Future.successful(Ok(successResponse(message.asInstanceOf[Success], data)))
              }
            case ResultSet(ResultTypeImpl.FAILURE, result) =>
              result match {
                case Result(message, _) =>
                  Future.successful(Ok(failedResponse(message.asInstanceOf[Failure])))
              }
            case ResultSet(ResultTypeImpl.ERROR, result) =>
              result match {
                case Result(message, _) =>
                  Future.successful(Ok(errorResponse(message.asInstanceOf[Error])))
              }
            case _ => Future.successful(BadRequest(errorResponse(UnknownError)))
          }
      )
    }
  }

  /**
  * User deletion handler
  *
  * The configuration in the `routes` file means that this method
  * will be called when the application receives a `DELETE` request with
  * a path of `/delete/user`.
  */
  def deleteUser(user_id: String): Action[AnyContent] = requestAuthAction.andThen(sessionAuthAction).async{
    implicit request: Request[AnyContent] =>
      users.deleteUser(user_id) match {
        case ResultSet(ResultTypeImpl.SUCCESS, result) =>
          result match {
            case Result(message, _) =>
              Future.successful(Ok(successResponse(message.asInstanceOf[Success], None)))
          }
        case ResultSet(ResultTypeImpl.FAILURE, result) =>
          result match {
            case Result(message, _) =>
              Future.successful(Ok(failedResponse(message.asInstanceOf[Failure])))
          }
        case ResultSet(ResultTypeImpl.ERROR, result) =>
          result match {
            case Result(message, _) =>
              Future.successful(Ok(errorResponse(message.asInstanceOf[Error])))
          }
        case _ => Future.successful(BadRequest(errorResponse(UnknownError)))
      }
  }

  def logout(): Action[AnyContent] = requestAuthAction.andThen(sessionAuthAction).async {
    implicit request: Request[AnyContent] =>
      val token = Token(request.session.get("token").getOrElse("null"))
      sessions.deleteSession(token).map(_ => Redirect("/").withNewSession)
  }
}
