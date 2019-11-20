package models

import com.typesafe.config.ConfigFactory

object Environment extends Enumeration {
  val development, test, production = Value

  def current : Environment.Value = {
    // Read environment from configuration
    Environment.withName(ConfigFactory.load.getString("server.environment"))
  }

}
