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

import currencytrade.api.RouteActor
import akka.actor.{ ActorRef, Cancellable }
import scala.concurrent.duration.DurationInt
import spray.can.Http
import spray.can.websocket.WebSocketServerWorker
import spray.can.websocket.frame.{ CloseFrame, PingFrame, PongFrame, StatusCode, TextFrame }
import spray.routing.Route
import spray.http.HttpRequest
import spray.routing.{ Rejected, RequestContext }

/**
 * WebSocket server implementation that handles the lifecycle events
 * of a web socket connection.
 *
 * This class is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
class WebSocketServer(val serverConnection: ActorRef, val route: Route) extends RouteActor with WebSocketServerWorker with WebSocket {
  import context.dispatcher
  //override lazy val connection = serverConnection
  lazy val connection = serverConnection
  override def receive: PartialFunction[Any, Unit] = matchRoute(route) orElse handshaking orElse closeLogic
  private def matchRoute(route: Route): Receive = {
    case request: HttpRequest =>
      val ctx = RequestContext(request, self, request.uri.path)
      log.debug("HTTP request for uri {}", request.uri.path)
      route(ctx.withResponder(self))
      handshaking(request)
    case WebSocket.Register(request, actor, ping) =>
      if (ping) pinger = Some(context.system.scheduler.scheduleOnce(110.seconds, self, WebSocket.Ping))
      handler = actor
      uripath = request.uri.path.toString
      handler ! WebSocket.Open(this)
    case Rejected(rejections) =>
      log.info("Rejecting with {}", rejections)
      context stop self
  }
  // this is the actor's behavior after the WebSocket handshaking resulted in an upgraded request
  override def businessLogic: Receive = {
    case TextFrame(message) => {
      ping
      handler ! WebSocket.Message(this, message.utf8String)
    }
    case WebSocket.Ping => send(PingFrame())
    case PongFrame(payload) => ping
    case Http.Aborted => handler ! WebSocket.Error(this, "aborted")
    case Http.ErrorClosed(cause) => handler ! WebSocket.Error(this, cause)
    case CloseFrame(status, reason) => handler ! WebSocket.Close(this, status.code, reason)
    case Http.Closed | WebSocket.Release => handler ! WebSocket.Close(this, StatusCode.NormalClose.code, "")
    case Http.ConfirmedClosed | Http.PeerClosed => handler ! WebSocket.Close(this, StatusCode.GoingAway.code, "")
    case whatever => log.debug("WebSocket received '{}'", whatever)
  }
  def send(message: String): Unit = send(TextFrame(message))
  def close: Unit = send(CloseFrame(StatusCode.NormalClose))
  def path: String = uripath
  private def ping(): Unit = pinger match {
    case None => // nothing to do
    case Some(timer) =>
      if (!timer.isCancelled) timer.cancel
      pinger = Some(context.system.scheduler.scheduleOnce(110.seconds, self, WebSocket.Ping))
  }
  private var uripath = "/"
  private var pinger: Option[Cancellable] = None
  private var handler = self
}
