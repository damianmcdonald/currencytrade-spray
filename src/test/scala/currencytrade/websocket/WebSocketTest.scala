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

import currencytrade.Configuration
import currencytrade.api.{ MainActors, CurrencyTradeApi, RootService }
import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import currencytrade.trade.TradeRetrieveService
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket.frame.TextFrame

@RunWith(classOf[JUnitRunner])
class WebSocketTest extends FunSuite with MainActors with CurrencyTradeApi {
  implicit lazy val system = ActorSystem("currencytrade-socket-WebSocketTest")
  sys.addShutdownHook({ system.shutdown })
  test("websocket connection") {
    val wss = system.actorOf(Props(new RootService[WebSocketServer](new TradeRetrieveService(notifier).wsroute)), "wswss")
    IO(UHttp) ! Http.Bind(wss, Configuration.host, Configuration.portWs)
    Thread.sleep(2000L) // wait for all servers to be cleanly started
    var wsmsg = ""
    val ws = system.actorOf(Props(new TestingWebSocketClient {
      override def businessLogic = {
        case WebSocket.Release => close
        case WebSocket.Send(msg) => wsmsg = msg
        case TextFrame(msg) => wsmsg = msg.utf8String
        case whatever => // ignore
      }
    }))
    ws ! WebSocket.Connect(Configuration.host, Configuration.portWs, "/v1/ws")
    Thread.sleep(1000L) // wait for connection
    ws ! WebSocket.Send("123456789")
    Thread.sleep(1000L) // wait for send
    assert(wsmsg.equals("123456789"))
    ws ! WebSocket.Release // wait for release
    Thread.sleep(1000L)
    IO(UHttp) ! Http.Unbind
    system.shutdown
  }
}
