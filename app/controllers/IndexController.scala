package controllers

import play.api.mvc.{ Action, AnyContent, Request }

import javax.inject.Inject
import scala.concurrent.ExecutionContext

/** The `Index` controller.
  */
class IndexController @Inject() (
    components: SilhouetteControllerComponents
  )(implicit
    ex: ExecutionContext
  ) extends SilhouetteController(components) {
  def ping: Action[AnyContent] = UnsecuredAction { implicit request: Request[AnyContent] =>
    Ok("Server is up!")
  }
}
