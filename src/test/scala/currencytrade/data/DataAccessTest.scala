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

package currencytrade.data

import com.mongodb.DBObject
import currencytrade.CurrencyTradeBase
import org.scalatest.FunSpec

class DataAccessTest extends FunSpec with CurrencyTradeBase {

  val dataAccess = new MongoDBDataAccess

  describe("A mongodb query") {
    describe("to retrieve the latest 10 currency trades") {
      it("should return a List of 10 DBObjects") {
        assert(dataAccess.getLatestTrades.nonEmpty, "latest 10 currency trades should not be empty")
        assert(dataAccess.getLatestTrades.size == 10, "latest 10 currency trades should contain 10 entries")
        assert(dataAccess.getLatestTrades().isInstanceOf[List[DBObject]], "latest 10 currency trades should be of type List[DBObject]")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve countries by trade volume") {
      it("should return a List of 10 DBObjects") {
        assert(dataAccess.getCountriesByTradeVolume.nonEmpty, "countries by trade volume should not be empty")
        assert(dataAccess.getCountriesByTradeVolume.size == 10, "countries by trade volume should contain 10 entries")
        assert(dataAccess.getCountriesByTradeVolume.isInstanceOf[List[DBObject]], "countries by trade volume should be of type List[DBObject]")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies sold by volume") {
      it("should return a List of 10 DBObjects") {
        assert(dataAccess.getCurrenciesSoldByVolume.nonEmpty, "currencies sold by volume should not be empty")
        assert(dataAccess.getCurrenciesSoldByVolume.size == 10, "currencies sold by volume should contain 10 entries")
        assert(dataAccess.getCurrenciesSoldByVolume.isInstanceOf[List[DBObject]], "currencies sold by volume should be of type List[DBObject]")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies sold by value") {
      it("should return a List of 10 DBObjects") {
        assert(dataAccess.getCurrenciesSoldByValue.nonEmpty, "currencies sold by value should not be empty")
        assert(dataAccess.getCurrenciesSoldByValue.size == 10, "currencies sold by value should contain 10 entries")
        assert(dataAccess.getCurrenciesSoldByValue.isInstanceOf[List[DBObject]], "currencies sold by value should be of type List[DBObject]")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies bought by volume") {
      it("should return a List of 10 DBObjects") {
        assert(dataAccess.getCurrenciesBoughtByVolume.nonEmpty, "currencies bought by volume should not be empty")
        assert(dataAccess.getCurrenciesBoughtByVolume.size == 10, "currencies bought by volume should contain 10 entries")
        assert(dataAccess.getCurrenciesBoughtByVolume.isInstanceOf[List[DBObject]], "currencies bought by volume should be of type List[DBObject]")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies bought by value") {
      it("should return a List of 10 DBObjects") {
        assert(dataAccess.getCurrenciesBoughtByValue.nonEmpty, "currencies bought by value should not be empty")
        assert(dataAccess.getCurrenciesBoughtByValue.size == 10, "currencies bought by value should contain 10 entries")
        assert(dataAccess.getCurrenciesBoughtByValue.isInstanceOf[List[DBObject]], "currencies bought by value should be of type List[DBObject]")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve country codes") {
      it("should return a List of DBObjects") {
        assert(dataAccess.getCountryCodes.nonEmpty, "country codes should not be empty")
        assert(dataAccess.getCountryCodes.isInstanceOf[List[String]], "country codes should be of type List[String]")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currency pairs by volume sold") {
      it("should return a List of 10 DBObjects") {
        assert(dataAccess.getCurrencyPairsByVolume.nonEmpty, "currency pairs by volume sold should not be empty")
        assert(dataAccess.getCurrencyPairsByVolume.size == 10, "currency pairs by volume sold should contain 10 entries")
        assert(dataAccess.getCurrencyPairsByVolume.isInstanceOf[List[DBObject]], "currency pairs by volume sold should be of type List[DBObject]")
      }
    }
  }

}
