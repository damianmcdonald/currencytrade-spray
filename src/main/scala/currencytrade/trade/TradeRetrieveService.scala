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

import akka.actor.{ ActorSystem, ActorRef }
import akka.event.slf4j.SLF4JLogging
import currencytrade.api.CoreAdditions
import currencytrade.convertors.DBObjectToJsonConvertor._
import currencytrade.websocket.WebSocket
import spray.routing.Directives
import currencytrade.notification.NotificationActor._
import spray.http.MediaTypes._
import scala.concurrent.Future

/**
 * API routes that handle trade information requests
 */
class TradeRetrieveService(actor: ActorRef)(implicit system: ActorSystem) extends Directives with CoreAdditions with SLF4JLogging {

  /**
   * Http API routes served by this class
   */
  lazy val route =
    pathPrefix("v1") {
      path("countriesvolume") {
        get {
          respondWithMediaType(`application/json`) {
            onSuccess(Future { dataAccess.getCountriesByTradeVolume() }) { result => complete(convert(TradeVolumeByCountry(result))) }
          }
        }
      } ~
        path("sellvolume") {
          get {
            respondWithMediaType(`application/json`) {
              onSuccess(Future { dataAccess.getCurrenciesSoldByVolume() }) { result => complete(convert(CurrenciesSoldByVolume(result))) }
            }
          }
        } ~
        path("sellvalue") {
          get {
            respondWithMediaType(`application/json`) {
              onSuccess(Future { dataAccess.getCurrenciesSoldByValue() }) { result => complete(convert(CurrenciesSoldByValue(result))) }
            }
          }
        } ~
        path("buyvolume") {
          get {
            respondWithMediaType(`application/json`) {
              onSuccess(Future { dataAccess.getCurrenciesBoughtByVolume() }) { result => complete(convert(CurrenciesBoughtByVolume(result))) }
            }
          }
        } ~
        path("buyvalue") {
          get {
            respondWithMediaType(`application/json`) {
              onSuccess(Future { dataAccess.getCurrenciesBoughtByValue() }) { result => complete(convert(CurrenciesBoughtByValue(result))) }
            }
          }
        } ~
        path("currencypair") {
          get {
            respondWithMediaType(`application/json`) {
              onSuccess(Future { dataAccess.getCurrencyPairsByVolume() }) { result => complete(convert(CurrencyPairsByVolume(result))) }
            }
          }
        } ~
        path("countrycodes") {
          get {
            respondWithMediaType(`application/json`) {
              onSuccess(Future { dataAccess.getCountryCodes() }) { result => complete(countryCodesJson(result)) }
            }
          }
        } ~
        path("latest") {
          get {
            respondWithMediaType(`application/json`) {
              onSuccess(Future { dataAccess.getLatestTrades() }) { result => complete(latestTadesJson(result)) }
            }
          }
        }
    }

  /**
   * Web socket API routes served by this class
   */
  lazy val wsroute =
    pathPrefix("v1") {
      path("ws") {
        implicit ctx => ctx.responder ! WebSocket.Register(ctx.request, actor, true)
      }
    }

}
