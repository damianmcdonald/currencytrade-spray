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

package currencytrade.notification

import com.mongodb.casbah.Imports._
import currencytrade.convertors.DBObjectToJsonConvertor._
import currencytrade.trade.Trade
import currencytrade.websocket.WebSocket
import akka.actor.{ Actor, ActorLogging }
import scala.collection.mutable
import currencytrade.notification.NotificationActor._

/**
 * Defines the messages used by the [[currencytrade.data.NotificationActorr]] class
 * to react to web socket notification requests.
 */
object NotificationActor {
  sealed trait NotificationMessage
  case class TradePersisted(t: Trade) extends NotificationMessage
  case class TradeVolumeByCountry(xs: List[DBObject]) extends NotificationMessage
  case class CurrenciesSoldByVolume(xs: List[DBObject]) extends NotificationMessage
  case class CurrenciesBoughtByVolume(xs: List[DBObject]) extends NotificationMessage
  case class CurrenciesSoldByValue(xs: List[DBObject]) extends NotificationMessage
  case class CurrenciesBoughtByValue(xs: List[DBObject]) extends NotificationMessage
  case class CurrencyPairsByVolume(xs: List[DBObject]) extends NotificationMessage
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
    case msg: NotificationMessage => clients.foreach(_.send(convert(msg)))
    case whatever => log.warning("Finding '{}'", whatever)
  }
}
