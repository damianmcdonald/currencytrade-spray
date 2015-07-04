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

import akka.actor.Actor
import akka.io.IO
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket.WebSocketClientWorker
import spray.can.websocket.frame.{ CloseFrame, StatusCode, TextFrame }
import spray.http.{ HttpHeaders, HttpMethods, HttpRequest }
import akka.actor.actorRef2Scala
import spray.http.Uri.apply

abstract class TestingWebSocketClient extends Actor with WebSocketClientWorker with WebSocket {
  override def receive = connect orElse handshaking orElse closeLogic
  private def connect(): Receive = {
    case WebSocket.Connect(host, port, resource, ssl) =>
      val headers = List(
        HttpHeaders.Host(host, port),
        HttpHeaders.Connection("Upgrade"),
        HttpHeaders.RawHeader("Upgrade", "websocket"),
        HttpHeaders.RawHeader("Sec-WebSocket-Version", "13"),
        HttpHeaders.RawHeader("Sec-WebSocket-Key", "x3JJHMbDL1EzLkh9GBhXDw==")
      )
      request = HttpRequest(HttpMethods.GET, resource, headers)
      IO(UHttp)(context.system) ! Http.Connect(host, port, ssl)
  }
  def send(message: String) = connection ! TextFrame(message)
  def close() = connection ! CloseFrame(StatusCode.NormalClose)
  def path() = uripath
  private var uripath = ""
  private var request: HttpRequest = null
  override def upgradeRequest = request
}
