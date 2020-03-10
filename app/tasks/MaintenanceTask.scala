/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package tasks

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.Logging
import utilities.Log

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class MaintenanceTask @Inject()(actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) extends Logging{

  val log = Log.get
  actorSystem.scheduler.schedule(initialDelay = 10.seconds, interval = 1.minute) {
    // the block of code that will be executed
    log.debug("Running maintenance task")
    models.KeyOperations.expire
  }
}