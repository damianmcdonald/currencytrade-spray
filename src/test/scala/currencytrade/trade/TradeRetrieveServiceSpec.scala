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
import spray.http.StatusCodes
import spray.testkit.Specs2RouteTest

class TradeRetrieveServiceSpec extends mutable.Specification with Specs2RouteTest with MainActors with CurrencyTradeApi with CurrencyTradeBase {

  def actorRefFactory = system

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/countriesvolume" in {
      Get("/v1/countriesvolume") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "COUNTRIES_VOLUME")
      }
    }
  }

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/currencypair" in {
      Get("/v1/currencypair") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "CURRENCY_PAIRS")
      }
    }
  }

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/countrycodes" in {
      Get("/v1/countrycodes") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "ORIGINATING_COUNTRIES")
      }
    }
  }

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/latest" in {
      Get("/v1/latest") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "LATEST_TRADES")
      }
    }
  }

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/sellvolume" in {
      Get("/v1/sellvolume") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "CURRENCIES_SOLD_VOLUME")
      }
    }
  }

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/sellvalue" in {
      Get("/v1/sellvalue") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "CURRENCIES_SOLD_VALUE")
      }
    }
  }

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/buyvolume" in {
      Get("/v1/buyvolume") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "CURRENCIES_BOUGHT_VOLUME")
      }
    }
  }

  "CurrencyTrade API" should {
    "return a json object for a GET request to path /v1/buyvalue" in {
      Get("/v1/buyvalue") ~> routes ~> check {
        status === StatusCodes.OK
        !responseAs[String].isEmpty
        responseDataMatches(responseAs[String])
        responseEventMatches(responseAs[String], "CURRENCIES_BOUGHT_VALUE")
      }
    }
  }

}
