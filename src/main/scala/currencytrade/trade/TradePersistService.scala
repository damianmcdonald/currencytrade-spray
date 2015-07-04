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

package currencytrade.trade

import currencytrade.api.CoreAdditions
import currencytrade.data.DataAccessActor
import currencytrade.notification.NotificationActor
import akka.actor.{ ActorRef, ActorSystem }
import spray.routing.Directives
import spray.http._
import MediaTypes._
import spray.httpx.unmarshalling._
import currencytrade.trade.TradeJsonProtocol._
import spray.httpx.SprayJsonSupport._
import akka.event.slf4j.SLF4JLogging
import scala.concurrent._

/**
 * API routes that handle trade persist requests
 */
class TradePersistService(actor: ActorRef)(implicit system: ActorSystem) extends Directives with CoreAdditions with SLF4JLogging {

  /**
   * API routes served by this class
   */
  lazy val route =
    pathPrefix("v1") {
      path("trade") {
        post {
          respondWithMediaType(`text/plain`) {
            entity(as[Trade]) { trade =>
              onSuccess(Future { dataAccess.persistTrade(Trade.buildMongoDbObjectFromTrade(trade)) }) { result =>
                log.debug("Database id: '{}' for persisted trade: '{}'", List(result, trade))
                // no need to re-query the database for the persisted trade id
                // send the value straight to the notifier actor
                system.actorSelection("/user/notifier") ! NotificationActor.TradePersisted(trade)
                // once a trade has been persisted, invoke the DataAccessActor to gather
                // the latest information about trades, which the DataAccessActor will pass on to
                // the NotificationActor in order to push web socket updates to registered client browsers
                actor ! DataAccessActor.CurrencyPairsVolume
                actor ! DataAccessActor.CurrenciesSoldVolume
                actor ! DataAccessActor.CurrenciesSoldValue
                actor ! DataAccessActor.CurrenciesBoughtVolume
                actor ! DataAccessActor.CurrenciesBoughtValue
                actor ! DataAccessActor.CountryTradeVolume
                // return the object id of the persisted trade to the browser
                complete(s"$result")
              }
            }
          }
        }
      }
    }

}
