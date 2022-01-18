package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.i18n.Lang
import play.api.mvc.{ Action, AnyContent, Request }
import utils.auth.{ JWTEnvironment, WithProvider }
import utils.json.Forms._
import utils.json.Message
import utils.json.Responses._

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import utils.json.Writes
import play.api.Configuration

/** The `Sign Up` controller.
  */
class UserAccountController @Inject() (
    components: SilhouetteControllerComponents,
    config: Configuration,
  )(implicit
    ex: ExecutionContext
  ) extends SilhouetteController(components) {
  private def validateApiKey(action: Action[AnyContent]) = Action.async(action.parser) { request =>
    request.headers.get("X-API-Key") match {
      case Some(k) =>
        if (k == config.get[String]("api.key")) action(request)
        else Future.successful(Forbidden(failedResponse(Message.InvalidApiKey)))
      case None => Future.successful(Forbidden(failedResponse(Message.ApiKeyNotFound)))
    }
  }

  /** Handles sign up request
    *
    * @return The result to display.
    */
  def signUp: Action[AnyContent] = validateApiKey {
    UnsecuredAction.async { implicit request: Request[AnyContent] =>
      implicit val lang: Lang = supportedLangs.availables.head
      createUserForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(Writes.UserFormWithErrorsWrites.writes(formWithErrors))),
          newUser =>
            userService.retrieve(LoginInfo(CredentialsProvider.ID, newUser.email)).flatMap {
              case Some(_) =>
                Future.successful(Conflict(failedResponse(Message.DuplicateUserEntry)))
              case None =>
                val authInfo = passwordHasherRegistry.current.hash(newUser.password)
                val user = newUser.copy(password = authInfo.password)
                userService
                  .save(user)
                  .map(u =>
                    Ok(successResponse(Message.UserCreateSuccessful, Some(u.copy(password = ""))))
                  )
            },
        )
    }
  }

  def signIn: Action[AnyContent] = validateApiKey {
    UnsecuredAction.async { implicit request: Request[AnyContent] =>
      implicit val lang: Lang = supportedLangs.availables.head
      authUserForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(Writes.RequestFormWithErrorsWrites.write(formWithErrors))),
          data => {
            val credentials = Credentials(data.email, data.password)
            credentialsProvider
              .authenticate(credentials)
              .flatMap { loginInfo =>
                userService.retrieve(loginInfo).flatMap {
                  case Some(user) =>
                    for {
                      authenticator <- authenticatorService.create(loginInfo)
                      token <- authenticatorService.init(authenticator)
                      result <- authenticatorService.embed(
                        token,
                        Ok(
                          successResponse(
                            Message.UserAuthSuccessful,
                            Some(user.copy(password = "")),
                          )
                        ),
                      )
                    } yield result

                  case None => Future.successful(Ok(failedResponse(Message.UserNotFound)))
                }
              }
              .recover {
                case _: ProviderException =>
                  Unauthorized(failedResponse(Message.InvalidCredentials))
              }
          },
        )
    }
  }

  def changePassword: Action[AnyContent] = validateApiKey {
    SecuredAction(
      WithProvider[AuthType](CredentialsProvider.ID)
    ).async { implicit request: SecuredRequest[JWTEnvironment, AnyContent] =>
      implicit val lang: Lang = supportedLangs.availables.head
      changePasswordForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(Writes.RequestFormWithErrorsWrites.write(formWithErrors))),
          data => {
            val credentials = Credentials(request.identity.email, data.oldPassword)
            credentialsProvider
              .authenticate(credentials)
              .flatMap { loginInfo =>
                val newHashedPassword =
                  passwordHasherRegistry.current.hash(data.newPassword)
                authInfoRepository
                  .update(loginInfo, newHashedPassword)
                  .map(_ => Ok(successResponse(Message.UserUpdateSuccessful, None)))
              }
              .recover {
                case _: ProviderException =>
                  Unauthorized(failedResponse(Message.InvalidCredentials))
              }
          },
        )
    }
  }

  def updateProfile(userID: String): Action[AnyContent] = validateApiKey {
    SecuredAction(
      WithProvider[AuthType](CredentialsProvider.ID)
    ).async { implicit request: SecuredRequest[JWTEnvironment, AnyContent] =>
      implicit val lang: Lang = supportedLangs.availables.head
      updateProfileForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(Writes.RequestFormWithErrorsWrites.write(formWithErrors))),
          data =>
            userService.getUserById(userID).flatMap {
              case Some(user) =>
                userService
                  .updateProfile(user.userID, data.newProfile)
                  .map(u =>
                    Ok(
                      successResponse(Message.UserUpdateSuccessful, Some(u.copy(password = "")))
                    )
                  )

              case None => Future.successful(Ok(failedResponse(Message.UserNotFound)))
            },
        )
    }
  }

  def deleteUser(userID: String): Action[AnyContent] = validateApiKey {
    SecuredAction(
      WithProvider[AuthType](CredentialsProvider.ID)
    ).async { implicit request: SecuredRequest[JWTEnvironment, AnyContent] =>
      implicit val lang: Lang = supportedLangs.availables.head
      userService.getUserById(userID).flatMap {
        case Some(user) =>
          userService
            .delete(user)
            .flatMap(user =>
              Future
                .successful(Ok(successResponse(Message.UserDeleteSuccessful, Some(user))))
            )

        case None => Future.successful(Ok(failedResponse(Message.UserNotFound)))
      }
    }
  }
}
