package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logging
import play.api.libs.json.Json
import play.filters.csrf.CSRF

import utilities._
import models._
import models.JsonStatus._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Logging {


  private val log = Log.get
  // A device to translate JSON object to a class;
  // keys in JSON must match class parameters.
  implicit val subscriber_form_reads = Json.reads[SubscriberForm]

  def index = Action { implicit request: Request[AnyContent] =>

    // Render view. CSRF token is passed to enable secure AJAX post from the html.
    Ok(views.html.index(CSRF.getToken.get.value))
  }

  def subscribe_json = Action { request =>

    try {

      // Get JSON from the POST request and translate to a class
      val json = request.body.asJson.get
      val _form = json.as[SubscriberForm]

      // Localhost: get IP from the request.
      // Production: get IP from the header passed by the proxy server
      val ip = {
        if (Server.environment == Environment.development) request.remoteAddress
        else request.headers.get("X-Real-IP").get
      }

      // Create a new subscriber
      SubscriberOperations.create(Subscriber(_form.email, ip, TimeStamp.UTC))

    }
    catch {
      case e: Exception => log.error(s"Error during subscription event: ${e.getMessage}")
    }

    // Return success response regardless
    Ok( JsonResponse.get(success, "Thank you!"))

  }




}
