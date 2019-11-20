package utilities

import com.typesafe.config.ConfigFactory
import models.Environment

object Server {

  // Get url and environment from configuration

  def url :String = {
    ConfigFactory.load.getString("server.url")
  }

  def environment : Environment.Value = {
    Environment.withName(ConfigFactory.load.getString("server.environment"))
  }

}
