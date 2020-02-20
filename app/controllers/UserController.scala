package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logging
import play.api.libs.json.Json
import models._
import models.JsonStatus._
import utilities.{Log, TimeStamp}

class UserController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Logging {

  private val log = Log.get
  // A device to translate JSON object to a class;
  // keys in JSON must match class parameters.
  implicit val user_form_reads = Json.reads[UserForm]

  def user_add_json = Action { request =>

    // Read the cookie and authenticate user
    UserOperations.authenticate_from_cookie(request.session.get("session")) match {
      case Some(authenticated_user) => {
        // User is authenticated
        if (authenticated_user.role == Role.administrator) {
          // User is an administrator
          // Get JSON from the POST request and translate to a class
          val json = request.body.asJson.get
          val _form = json.as[UserForm]

          // Get user
          UserOperations.read(_form.email) match {
            case Some(user) => {
              // This user already exits; return JSON response.
              Ok(JsonResponse.get(failure,s"User ${user.email} already exists"))

            }
            case None => {
              // Create new user
              val new_user = User(
                _form.email,
                _form.name,
                Role.withName(_form.role),
                null,
                TimeStamp.UTC
              )
              UserOperations.create(new_user)

              // Return JSON response
              Ok( JsonResponse.get(success,s"${new_user.name} has been added. An email with login information is on the way to ${new_user.email}.") )

            }

          }
        }
        else Redirect("/signin") // User is not an administrator
      }
      case None => Redirect("/signin") // User is not authenticated
    }

  }

  def user_edit_json = Action { request =>

    // Read the cookie and authenticate user
    UserOperations.authenticate_from_cookie(request.session.get("session")) match {
      case Some(authenticated_user) => {
        // User is authenticated
        if (authenticated_user.role == Role.administrator) {
          // User is an administrator
          // Get JSON from the POST request and translate to a class
          val json = request.body.asJson.get
          val _form = json.as[UserForm]

          // Get user
          UserOperations.read(_form.email) match {
            case Some(exiting_user) => {
              // Make a copy of the user with updated name and role values
              val updated_user = exiting_user.copy(exiting_user.email, _form.name, Role.withName(_form.role))
              // Update user
              UserOperations.update(updated_user)
              // Return JSON response
              Ok( JsonResponse.get(success,s"${_form.name} has been updated.") )
            }
            case None => {
              // User not found
              // Return JSON response
              Ok( JsonResponse.get(failure, s"User with email ${_form.email} not found.") )

            }
          }
        }
        else Redirect("/signin")  // User is not an administrator
      }
      case None => Redirect("/signin") // User is not authenticated
    }

  }

  def user_delete_json = Action { request =>
    // Read the cookie and authenticate user
    UserOperations.authenticate_from_cookie(request.session.get("session")) match {
      // User is authenticated
      case Some(authenticated_user) => {
        if (authenticated_user.role == Role.administrator) {
          // User is an administrator
          // Get JSON from the POST request and translate to a class
          val json = request.body.asJson.get
          val _form = json.as[UserForm]

          // Get user
          UserOperations.read(_form.email) match {
            case Some(exiting_user) => {
              // Delete user
              UserOperations.delete(exiting_user)
              // Return JSON response
              Ok( JsonResponse.get(success, s"${_form.name} has been deleted.") )

            }
            case None => {
              // User not found
              // Return JSON response
              Ok ( JsonResponse.get(failure, s"User with email ${_form.email} not found.") )

            }
          }
        }
        else Redirect("/signin")  // User is not an administrator
      }
      case None => Redirect("/signin") // User is not authenticated
    }

  }

}