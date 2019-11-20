package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logging
import play.api.libs.json.Json
import play.filters.csrf.CSRF

import models._
import models.JsonStatus._

class AuthenticationController  @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Logging {

  // A device to translate JSON object to a class;
  // keys in JSON must match class parameters.
  implicit val reset_form_reads = Json.reads[UserHashForm]
  implicit val forgot_form_reads = Json.reads[UserForgotForm]
  implicit val authenticate_form_reads = Json.reads[UserAuthenticateForm]

  def signin = Action { implicit request =>

    // Render view. CSRF token is passed to enable secure AJAX post from the html.
    Ok(views.html.signin(CSRF.getToken.get.value))
  }

  def signout = Action { request =>

    // Delete session and redirect to sign in view
    Redirect("/signin").withNewSession
  }

  def authenticate_json = Action { request =>

    // Get JSON from the POST request and translate to a class
    val json = request.body.asJson.get
    val _form = json.as[UserAuthenticateForm]

    // Authenticate user with email and password
    UserOperations.authenticate(_form.email, _form.password) match {
      case Some(user) => {
        // User has been authenticated, issue a session
        val session_token = SessionOperations.create(user).token

        // Write session to cookie and write JSON response
        Ok( JsonResponse.get(success,"User authenticated") )
          .withSession("session" -> session_token)
      }
      case None => {
        // User not authenticated, do not issue a session
        Ok( JsonResponse.get(failure,"User not authenticated") )
      }
    }
  }


  def reset(token: String) = Action { implicit request =>
    // Validate reset key from the request
    KeyOperations.validate(token) match {
      case Some(valid_token) => {
        // Key is valid, render Reset view.
        // CSRF token is passed to enable secure AJAX post from the html.
        Ok(views.html.reset(valid_token.token, CSRF.getToken.get.value))

      }
      case None => {
        // Key is invalid. Render Expired view.
        Ok(views.html.expired())
      }
    }


  }

  def hash_json = Action { request =>

    // Get JSON from the POST request and translate to a class
    val json = request.body.asJson.get
    val _form = json.as[UserHashForm]

    // Encrypt user's password and store the resulting hash
    UserOperations.hash(_form.token, _form.password) match {

      case Some(authenticated_user) => {
        // Once the hash has been created, the user is authenticated by default.
        // Issue a session
        val session_token = SessionOperations.create(authenticated_user).token
        // Write session to cookie and return JSON response
        Ok( JsonResponse.get(success,"Hash generated") )
          .withSession("session" -> session_token)
      }
      case None => {
        // Hashing operation failed
        Ok( JsonResponse.get(failure,"Error generating hash") )
      }
    }

  }

  def forgot = Action { implicit request =>
    // Render view. CSRF token is passed to enable secure AJAX post from the html.
    Ok(views.html.forgot(CSRF.getToken.get.value))
  }

  def forgot_json = Action { request =>

    // Get JSON from the POST request and translate to a class
    val json = request.body.asJson.get
    val _form = json.as[UserForgotForm]

    UserOperations.read(_form.email) match {
      case Some(user) => {
        // User located by email, send a password reset link.
        UserOperations.send_reset_link(user)
        // Return JSON response
        Ok( JsonResponse.get(success,"Reset link sent") )

      }
      case None => {
        // User not found by email
        // Return JSON response
        Ok( JsonResponse.get(failure,"Error sending reset link") )

      }
    }

  }

}
