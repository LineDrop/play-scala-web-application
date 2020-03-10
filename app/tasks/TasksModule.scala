/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package tasks

import play.api.inject.{SimpleModule, _}

class TasksModule extends SimpleModule(bind[MaintenanceTask].toSelf.eagerly())