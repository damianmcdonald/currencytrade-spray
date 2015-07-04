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

import currencytrade.CurrencyTradeBase
import currencytrade.api.{ CurrencyTradeApi, MainActors }
import org.specs2.mutable
import spray.http.{ StatusCodes, MediaTypes, HttpEntity }
import spray.testkit.Specs2RouteTest

class TradePersistServiceSpec extends mutable.Specification with Specs2RouteTest with MainActors with CurrencyTradeApi with CurrencyTradeBase {

  def actorRefFactory = system

  "CurrencyTrade API" should {
    "return an object id for a POST request to path /v1/trade" in {
      Post("/v1/trade").withEntity(HttpEntity(MediaTypes.`application/json`, tradeAsString)) ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseIsObjectId(responseAs[String])
      }
    }
  }

  "CurrencyTrade API" should {
    "return a 404 Not Found error for a POST request to path /v1/trade that contains a malformed trade" in {
      Post("/v1/trade").withEntity(HttpEntity(MediaTypes.`application/json`, tradeAsStringMalformed)) ~> routes ~> check {
        status === StatusCodes.NotFound
      }
    }
  }

  "CurrencyTrade API" should {
    "return a 404 Not Found error for a POST request to path /v1/trade that does not contain a proper content type" in {
      Post("/v1/trade").withEntity(HttpEntity(tradeAsString)) ~> routes ~> check {
        status === StatusCodes.NotFound
      }
    }
  }

  "CurrencyTrade API" should {
    "return a 404 Not Found error for a GET request to path /v1/trade" in {
      Get("/v1/trade").withEntity(HttpEntity(MediaTypes.`application/json`, tradeAsString)) ~> routes ~> check {
        status === StatusCodes.NotFound
      }
    }
  }

}
