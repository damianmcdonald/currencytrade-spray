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

package currencytrade.convertors

import currencytrade.notification.NotificationActor._
import currencytrade.convertors.DBObjectToJsonConvertor._
import org.scalatest.FunSpec
import currencytrade.CurrencyTradeBase

class DBObjectToJsonConvertorTest extends FunSpec with CurrencyTradeBase {

  describe("A List of DBObjects") {
    describe("when containing data about currency sales volume by country") {
      it("should return a valid Json response") {
        val xs = countriesVolume
        val json = countriesVolumeResult
        val conversion = convert(TradeVolumeByCountry(xs))
        assert(conversion.isInstanceOf[String], "currency sales volume by country should be of type Stringt")
        assert(conversion.toString.equals(json), "currency sales volume by country conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about currency pairs by sales volume") {
      it("should return a valid Json response") {
        val xs = currencyPairs
        val json = currencyPairsResult
        val conversion = convert(CurrencyPairsByVolume(xs))
        assert(conversion.isInstanceOf[String], "currency pairs by sales volume should be of type String")
        assert(conversion.toString.equals(json), "currency pairs by sales volume conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about currencies sold by volume") {
      it("should return a valid Json response") {
        val xs = currenciesSoldVolume
        val json = currenciesSoldVolumeResult
        val conversion = convert(CurrenciesSoldByVolume(xs))
        assert(conversion.isInstanceOf[String], "currencies sold by volume should be of type String")
        assert(conversion.toString.equals(json), "currencies sold by volume conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about currencies sold by value") {
      it("should return a valid Json response") {
        val xs = currenciesSoldValue
        val json = currenciesSoldValueResult
        val conversion = convert(CurrenciesSoldByValue(xs))
        assert(conversion.isInstanceOf[String], "currencies sold by value should be of type String")
        assert(conversion.toString.equals(json), "currencies sold by value conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about currencies bought by volume") {
      it("should return a valid Json response") {
        val xs = currenciesBoughtVolume
        val json = currenciesBoughtVolumeResult
        val conversion = convert(CurrenciesBoughtByVolume(xs))
        assert(conversion.isInstanceOf[String], "currencies bought by volume should be of type String")
        assert(conversion.toString.equals(json), "currencies bought by volume conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about currencies bought by value") {
      it("should return a valid Json response") {
        val xs = currenciesBoughtValue
        val json = currenciesBoughtValueResult
        val conversion = convert(CurrenciesBoughtByValue(xs))
        assert(conversion.isInstanceOf[String], "currencies bought by value should be of type String")
        assert(conversion.toString.equals(json), "currencies bought by value conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about latest currency trades") {
      it("should return a valid Json response") {
        val xs = latestTrades
        val json = latestTradesResult
        val conversion = latestTadesJson(xs)
        assert(conversion.isInstanceOf[String], "latest currency trades should be of type String")
        assert(conversion.toString.equals(json), "latest currency trades conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about a persisted trade") {
      it("should return a valid Json response") {
        val xs = trade
        val json = tradePersistedResult
        val conversion = convert(TradePersisted(trade))
        assert(conversion.isInstanceOf[String], "persisted trade should be of type String")
        assert(stripString(conversion.toString).equals(json), "persisted trade conversion does not match expected response")
      }
    }
  }

  describe("A List of DBObjects") {
    describe("when containing data about country codes") {
      it("should return a valid Json response") {
        val xs = countryCodes
        val json = countryCodeResults
        val conversion = countryCodesJson(xs)
        assert(conversion.isInstanceOf[String], "country codes should be of type String")
        assert(conversion.toString.equals(json), "country codes conversion does not match expected response")
      }
    }
  }

}
