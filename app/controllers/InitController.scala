package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logging
import com.typesafe.config.ConfigFactory
import models._
import models.JsonStatus._
import utilities.{Log, TimeStamp}

class InitController  @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Logging {

  val log = Log.get

  def init(key: String) = Action { implicit request: Request[AnyContent] =>

    log.info("Initializing super administrator")

    // Load key from config
    val sa_key = ConfigFactory.load.getString("sa.key")

    // Check if the key in the request matches config
    if(key == sa_key) {

      // Create SA user
      val name = ConfigFactory.load.getString("sa.name")
      val email = ConfigFactory.load.getString("sa.email")

      UserOperations.create(
        User(
          email,
          name,
          Role.administrator,
          null,
          TimeStamp.UTC
        )
      )

    }

    // Return response
    Ok( JsonResponse.get(success,"Thank you! Application has been initialized.") )
  }

}
