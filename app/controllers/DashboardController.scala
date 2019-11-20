package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logging
import play.api.libs.json.Json
import play.filters.csrf.CSRF

import models._

class DashboardController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Logging {

  // A device to translate JSON object to a class;
  // keys in JSON must match class parameters.
  implicit val user_form_reads = Json.reads[UserForm]

  def subscribers = Action { implicit request: Request[AnyContent] =>

    // Read the cookie and authenticate user
    UserOperations.authenticate_from_cookie(request.session.get("session")) match {
      case Some(user) => {
        // User is authenticated, read data to be displayed
        val subscribers = SubscriberOperations.read
        val count_by_date = SubscriberOperations.count_by_date(subscribers)
        // Pass required data and render the view
        Ok(views.html.secure.subscribers(subscribers, count_by_date, user.role))
      }
      // User not authenticated, redirect to sign in
      case None => Redirect("/signin")
    }

  }

  def users = Action { implicit request: Request[AnyContent] =>

    // Read the cookie and authenticate user
    UserOperations.authenticate_from_cookie(request.session.get("session")) match {
      case Some(user) => {
        // User is authenticated
        if (user.role == Role.administrator) {
          // User is an administrator
          // Read data
          val users = UserOperations.read_all
          // Pass required data and render the view
          // CSRF token is passed to enable secure AJAX post from the html.
          Ok(views.html.secure.users(users, CSRF.getToken.get.value))

        }
        // User is not an administrator; redirect to sign in page
        else Redirect("/signin")
      }
        // User is not authenticated; redirect to sign in page
      case None => Redirect("/signin")
    }

  }

}