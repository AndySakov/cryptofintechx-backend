package auth

import api.misc.Message.{AuthTokenNotFound, InvalidAuthToken}
import api.misc.Responses.failedResponse
import pdi.jwt._
import play.api.http.HeaderNames
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

// A custom request type to hold our JWT claims, we can pass these on to the
// handling action
case class ClientRequest[A](jwt: JwtClaim, token: String, request: Request[A]) extends WrappedRequest[A](request)

// Our custom action implementation
class RequestAuthAction @Inject()(bodyParser: BodyParsers.Default, authService: AuthService)(implicit ec: ExecutionContext)
  extends ActionBuilder[ClientRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser
  override protected def executionContext: ExecutionContext = ec

  // A regex for parsing the Authorization header value
  private val headerTokenRegex = """Bearer (.+?)""".r

  // Called when a request is invoked. We should validate the bearer token here
  // and allow the request to proceed if it is valid.
  override def invokeBlock[A](request: Request[A], block: ClientRequest[A] => Future[Result]): Future[Result] =
    extractBearerToken(request) map { token =>
      authService.validateJwt(token) match {
        case Success(claim) => block(ClientRequest(claim, token, request))      // token was valid - proceed!
        case Failure(t) => Future.successful(Results.Unauthorized(failedResponse(InvalidAuthToken(t.getMessage))))  // token was invalid - return 401
      }
    } getOrElse Future.successful(Results.Unauthorized(failedResponse(AuthTokenNotFound)))     // no token was sent - return 401

  // Helper for extracting the token value
  private def extractBearerToken[A](request: Request[A]): Option[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) collect {
      case headerTokenRegex(token) => token
    }
}