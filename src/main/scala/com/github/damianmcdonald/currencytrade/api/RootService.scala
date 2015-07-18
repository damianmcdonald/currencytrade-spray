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

import akka.actor.{ ActorLogging, ActorRef, Props }
import spray.can.Http
import spray.routing.{ HttpServiceActor, Route }

import scala.reflect.ClassTag

/**
 * Defines base behaviours for Http API handling
 *
 * This class is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
class RootService[RA <: RouteActor](val route: Route)(implicit tag: ClassTag[RA]) extends HttpServiceActor with ActorLogging {
  override def receive: PartialFunction[Any, Unit] = {
    case connected: Http.Connected =>
      // implement the "per-request actor" pattern
      sender ! Http.Register(context.actorOf(Props(tag.runtimeClass, sender, route)))
    case whatever => log.debug("RootService got some {}", whatever)
  }
}

/**
 * Base trait for any route handling service
 *
 * This trait is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
trait RouteActor extends HttpServiceActor {
  def connection: ActorRef
  def route: Route
}

/**
 * Base trait for any route actor
 *
 * This trait is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
private[api] class BasicRouteActor(val connection: ActorRef, val route: Route) extends RouteActor {
  override def receive: PartialFunction[Any, Unit] = runRoute(route)
}
