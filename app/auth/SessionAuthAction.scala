package auth

import api.misc.Responses._
import api.misc.SessionTypeImpl
import api.misc.SessionTypeImpl.SessionType
import dao.SessionDAO
import models.Token
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// A custom request type to hold our JWT claims, we can pass these on to the
// handling action
case class SessionAuthRequest[A](request: Request[A], sessionType: SessionType) extends WrappedRequest[A](request)

// Our custom action implementation
class SessionAuthAction @Inject()(bodyParser: BodyParsers.Default, sessions: SessionDAO)(implicit ec: ExecutionContext)
  extends ActionBuilder[SessionAuthRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  // Called when a request is invoked. We should validate the request here
  // and allow the request to proceed if it is valid.
  override def invokeBlock[A](request: Request[A], block: SessionAuthRequest[A] => Future[Result]): Future[Result] =
    authSession(request) match {
      case Some(authRequest) if authRequest == SessionAuthRequest(authRequest, SessionTypeImpl.VALID) => block(authRequest)
      case Some(authRequest) if authRequest == SessionAuthRequest(authRequest, SessionTypeImpl.EXPIRED) =>
        Future.successful(sessionExpired())
      case Some(authRequest) if authRequest == SessionAuthRequest(authRequest, SessionTypeImpl.NOT_FOUND) =>
        Future.successful(sessionNotFound())
      case Some(_) =>
        Future.successful(sessionNotFound())
      case None =>
        Future.successful(sessionNotFound())
    }


  // Helper for extracting the token value
  private def authSession[A](request: Request[A]): Option[SessionAuthRequest[A]] = {
    val token = Token(request.session.get("token").getOrElse("null"))
    Some(SessionAuthRequest(request, sessions.validateSession(token)))
  }
}