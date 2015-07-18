/*
 * Copyright 2015 Damian McDonald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.damianmcdonald.currencytrade.api

import akka.actor.Props
import com.github.damianmcdonald.currencytrade.data.DataAccessActor
import com.github.damianmcdonald.currencytrade.notification.NotificationActor

/**
 * Trait to mixin [[akka.actor.Actor]] instances.
 *
 * This trait is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
trait MainActors {
  this: AbstractSystem =>

  lazy val notifier = system.actorOf(Props[NotificationActor], "notifier")
  lazy val dataAccess = system.actorOf(Props[DataAccessActor], "data-access")
}
