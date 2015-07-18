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

package com.github.damianmcdonald.currencytrade.notification

import akka.actor.{ Actor, ActorLogging }
import com.github.damianmcdonald.currencytrade.notification.NotificationActor._
import com.github.damianmcdonald.currencytrade.websocket.WebSocket

import scala.collection.mutable

/**
 * Defines the messages used by the NotificationActor class
 * to react to web socket notification requests.
 */
object NotificationActor {
  sealed trait NotificationMessage
  case class JsonMessage(json: String) extends NotificationMessage
}

/**
 * Actor that handles requests for web socket notifications.
 */
class NotificationActor extends Actor with ActorLogging {
  /** The list of clients registered for web socket notifications */
  val clients = mutable.ListBuffer[WebSocket]()

  /**
   * Receives messages and takes the appropriate web socket response
   */
  override def receive: PartialFunction[Any, Unit] = {
    case WebSocket.Open(ws) =>
      if (Option(ws).isDefined) {
        clients += ws
        log.debug("registered monitor for url {}", ws.path)
      }
    case WebSocket.Close(ws, code, reason) =>
      clients -= ws
      log.debug("WebSocket closed. Code: '{}', reason: '{}'", code, reason)
    case WebSocket.Error(ws, ex) =>
      log.debug("WebSocket error. Error: '{}'", ex)
    case JsonMessage(json) => clients.foreach(_.send(json))
    case whatever => log.warning("Finding '{}'", whatever)
  }
}
