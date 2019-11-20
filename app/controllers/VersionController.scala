package controllers

import javax.inject._
import play.api.mvc._
import play.api.Logging
import com.typesafe.config.ConfigFactory
import models._
import models.JsonStatus._
import utilities._

class VersionController  @Inject()(cc: ControllerComponents) extends AbstractController(cc) with Logging {

  val log = Log.get

  def version = Action {request: Request[AnyContent] =>

    log.debug("Getting version")

    // Load version from config
    val version = ConfigFactory.load.getString("version")

    // Return response
    Ok( JsonResponse.get(success,s"Version: $version") )
  }

}
