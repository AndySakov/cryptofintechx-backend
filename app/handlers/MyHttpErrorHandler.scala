package handlers

import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._
import utils.json.Responses._
import utils.json.Message
import io.sentry.Sentry

@Singleton
class MyHttpErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router],
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
  override def onProdServerError(
      request: RequestHeader,
      exception: UsefulException,
    ): Future[Result] = {
      Sentry.captureException(exception)
    Future.successful(
      InternalServerError("A server error occurred while we were processing your request")
    )
  }

  override def onForbidden(request: RequestHeader, message: String): Future[Result] =
    Future.successful(
      Forbidden(errorResponse(Message.ForbiddenError))
    )

  override def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(
      NotFound(errorResponse(Message.PathNotFound(request.path)))
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Sentry.captureException(exception)
    exception match {
      case tx: TimeoutException =>
        Future.successful(
          InternalServerError(serverErrorResponse(tx))
        )
      case _ => super.onServerError(request, exception)
    }
  }
}
