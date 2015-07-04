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

package currencytrade.xrates

import org.scalatest.FunSpec
import currencytrade.mock.MockCountryData

class XRatesTest extends FunSpec {

  val xRates = new OpenExchangeRates

  describe("A currency") {
    describe("when contained with the list of valid exchange rate currencies") {
      it("should return true") {
        assert(xRates.isCurrencySupported(MockCountryData("US", "USD")) == true, "USD currency should be supported")
      }
    }
  }

  describe("A currency") {
    describe("when not contained with the list of valid exchange rate currencies") {
      it("should return false") {
        assert(xRates.isCurrencySupported(MockCountryData("XX", "XXX")) == false, "XXX currency should not be supported")
      }
    }
  }

  describe("A valid currency e.g. AUD") {
    describe("when contained with the list of valid exchange rate currencies") {
      it("should return a specific exchange rate value e.g. 1.298912") {
        assert(xRates.getRateByCurrency("AUD") == Option(1.298912), "AUD currency should have an exchange rate of Option(1.298912)")
      }
    }
  }

  describe("A currency e.g. XXX") {
    describe("when not contained with the list of valid exchange rate currencies") {
      it("should return an Option.None") {
        assert(xRates.getRateByCurrency("XXX") == None, "XXX currency should not have an exchange and should return None")
      }
    }
  }

  describe("A valid currency e.g. TRY") {
    describe("and an associated currency value e.g. 5375.82") {
      it("should return a new value that has been calculated against a base currency e.g. USD") {
        assert(
          xRates.convertToBaseCurrency("TRY", new java.math.BigDecimal("5375.82")) == Option(new java.math.BigDecimal("14345.95")),
          "TRY currency should return a value of Option(BigDecimal(14345.95)) for a value of BigDecimal(5375.82)"
        )
      }
    }
  }

  describe("An invalid currency e.g. XXX") {
    describe("when not contained with the list of valid exchange rate currencies for base rate conversion") {
      it("should return an Option.None") {
        assert(xRates.convertToBaseCurrency("XXX", new java.math.BigDecimal("5375.82")) == None, "XXX currency should not have an exchange value and should return None")
      }
    }
  }

}
