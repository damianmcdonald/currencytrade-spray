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

package currencytrade.websocket

import akka.actor.ActorRef
import spray.http.HttpRequest

/**
 * Defines WebSocket server events that can are evaluated in the
 * receive method of [[currencytrade.websocket.WebSocketServer]]
 *
 * This object is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
object WebSocket {
  sealed trait WebSocketMessage
  case class Open(ws: WebSocket) extends WebSocketMessage
  case class Message(ws: WebSocket, msg: String) extends WebSocketMessage
  case class Close(ws: WebSocket, code: Int, reason: String) extends WebSocketMessage
  case class Error(ws: WebSocket, reason: String) extends WebSocketMessage
  case class Connect(host: String, port: Int, resource: String, withSsl: Boolean = false) extends WebSocketMessage
  case class Send(msg: String) extends WebSocketMessage
  case object Release extends WebSocketMessage
  case class Register(request: HttpRequest, handler: ActorRef, autoping: Boolean = false)
  private[websocket] object Ping extends WebSocketMessage
}

/**
 * Defines the actions and values of a WebSocket
 *
 * This trait is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
trait WebSocket {
  def send(message: String): Unit
  def close(): Unit
  def path(): String
}
