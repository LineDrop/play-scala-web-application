package utilities

import com.typesafe.config.ConfigFactory
import play.api.Logger

object Log {
  private val log_name = ConfigFactory.load.getString("log.name")

  def get : Logger = {
    Logger(log_name)

  }
}
