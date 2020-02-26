import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.Singleton
import play.api.Logging
import utilities.Log

@Singleton
class ErrorHandler extends HttpErrorHandler with Logging {

  val log = Log.get
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)(views.html.not_found())
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful( {
      log.error(s"${exception.toString}")
      // Send email alert
      val html_message = s"<html><body><p>Greetings! Error occurred in home application. Details below.</p><p> ${exception.getMessage}</p></body></html>"
      utilities.SendGrid.send("paul@linedrop.io", html_message, "Home app error")
      InternalServerError(views.html.error())
    }
    )
  }
}
