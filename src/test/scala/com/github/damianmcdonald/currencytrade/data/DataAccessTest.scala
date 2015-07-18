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

package com.github.damianmcdonald.currencytrade.data

import com.mongodb.DBObject
import com.github.damianmcdonald.currencytrade.CurrencyTradeBase
import org.scalatest.FunSpec
import net.liftweb.json._

class DataAccessTest extends FunSpec with CurrencyTradeBase {

  implicit val formats = net.liftweb.json.DefaultFormats

  val dataAccess = new MongoDBDataAccess

  describe("A mongodb query") {
    describe("to retrieve the latest 10 currency trades") {
      it("should return a valid Json String") {
        val result = dataAccess.getLatestTrades
        assert(result.nonEmpty, "latest 10 currency trades should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("LATEST_TRADES"), eventValue + " does not equal LATEST_TRADES")
        val strValue = {
          val xs = for { JString(x) <- (parsedJson \ "data" \ "currencyFrom") } yield x
          xs match {
            case x :: xs => x.toString
            case Nil => fail("No currencyFrom field found!")
          }
        }
        assert(strValue.nonEmpty, "parsed json value should not be empty")
        assert(strValue.length == 3, "parsed json value length should == 3")
        intercept[NumberFormatException] {
          // expected this exception as String can not be parsed to Int
          strValue.toInt
        }
        val numberValue = {
          val xs = for { JDouble(x) <- (parsedJson \ "data" \ "rate") } yield x
          xs match {
            case x :: xs => x.toString.toDouble // will fail if not a number
            case Nil => fail("No rate field found!")
          }
        }
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve countries by trade volume") {
      it("should return a valid Json String") {
        val result = dataAccess.getCountriesByTradeVolume
        assert(result.nonEmpty, "countries by trade volume should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("COUNTRIES_VOLUME"), eventValue + " does not equal COUNTRIES_VOLUME")
        val strValue = {
          val xs = for { JString(x) <- (parsedJson \ "data" \ "country") } yield x
          xs match {
            case x :: xs => x.toString
            case Nil => fail("No country field found!")
          }
        }
        assert(strValue.nonEmpty, "parsed json value should not be empty")
        assert(strValue.length == 2, "parsed json value length should == 2")
        intercept[NumberFormatException] {
          // expected this exception as String can not be parsed to Int
          strValue.toInt
        }
        val numberValue = {
          val xs = for { JInt(x) <- (parsedJson \ "data" \ "volume") } yield x
          xs match {
            case x :: xs => x.toString.toInt // will fail if not a number
            case Nil => fail("No volume field found!")
          }
        }
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies sold by volume") {
      it("should return a valid Json String") {
        val result = dataAccess.getCurrenciesSoldByVolume
        assert(result.nonEmpty, "currencies sold by volume should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("CURRENCIES_SOLD_VOLUME"), eventValue + " does not equal CURRENCIES_SOLD_VOLUME")
        val strValue = {
          val xs = for { JString(x) <- (parsedJson \ "data" \ "currency") } yield x
          xs match {
            case x :: xs => x.toString
            case Nil => fail("No currency field found!")
          }
        }
        assert(strValue.nonEmpty, "parsed json value should not be empty")
        assert(strValue.length == 3, "parsed json value length should == 3")
        intercept[NumberFormatException] {
          // expected this exception as String can not be parsed to Int
          strValue.toInt
        }
        val numberValue = {
          val xs = for { JInt(x) <- (parsedJson \ "data" \ "volume") } yield x
          xs match {
            case x :: xs => x.toString.toInt // will fail if not a number
            case Nil => fail("No volume field found!")
          }
        }
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies sold by value") {
      it("should return a valid Json String") {
        val result = dataAccess.getCurrenciesSoldByValue
        assert(result.nonEmpty, "currencies sold by value should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("CURRENCIES_SOLD_VALUE"), eventValue + " does not equal CURRENCIES_SOLD_VALUE")
        val strValue = {
          val xs = for { JString(x) <- (parsedJson \ "data" \ "currency") } yield x
          xs match {
            case x :: xs => x.toString
            case Nil => fail("No currency field found!")
          }
        }
        assert(strValue.nonEmpty, "parsed json value should not be empty")
        assert(strValue.length == 3, "parsed json value length should == 3")
        intercept[NumberFormatException] {
          // expected this exception as String can not be parsed to Int
          strValue.toInt
        }
        val numberValue = {
          val xs = for { JDouble(x) <- (parsedJson \ "data" \ "value") } yield x
          xs match {
            case x :: xs => x.toString.toDouble // will fail if not a number
            case Nil => fail("No value field found!")
          }
        }
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies bought by volume") {
      it("should return a valid Json String") {
        val result = dataAccess.getCurrenciesBoughtByVolume
        assert(result.nonEmpty, "currencies bought by volume should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("CURRENCIES_BOUGHT_VOLUME"), eventValue + " does not equal CURRENCIES_BOUGHT_VOLUME")
        val strValue = {
          val xs = for { JString(x) <- (parsedJson \ "data" \ "currency") } yield x
          xs match {
            case x :: xs => x.toString
            case Nil => fail("No currency field found!")
          }
        }
        assert(strValue.nonEmpty, "parsed json value should not be empty")
        assert(strValue.length == 3, "parsed json value length should == 3")
        intercept[NumberFormatException] {
          // expected this exception as String can not be parsed to Int
          strValue.toInt
        }
        val numberValue = {
          val xs = for { JInt(x) <- (parsedJson \ "data" \ "volume") } yield x
          xs match {
            case x :: xs => x.toString.toInt // will fail if not a number
            case Nil => fail("No volume field found!")
          }
        }
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currencies bought by value") {
      it("should return a valid Json String") {
        val result = dataAccess.getCurrenciesBoughtByValue
        assert(result.nonEmpty, "currencies bought by value should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("CURRENCIES_BOUGHT_VALUE"), eventValue + " does not equal CURRENCIES_BOUGHT_VALUE")
        val strValue = {
          val xs = for { JString(x) <- (parsedJson \ "data" \ "currency") } yield x
          xs match {
            case x :: xs => x.toString
            case Nil => fail("No currency field found!")
          }
        }
        assert(strValue.nonEmpty, "parsed json value should not be empty")
        assert(strValue.length == 3, "parsed json value length should == 3")
        intercept[NumberFormatException] {
          // expected this exception as String can not be parsed to Int
          strValue.toInt
        }
        val numberValue = {
          val xs = for { JDouble(x) <- (parsedJson \ "data" \ "value") } yield x
          xs match {
            case x :: xs => x.toString.toDouble // will fail if not a number
            case Nil => fail("No value field found!")
          }
        }
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve country codes") {
      it("should return a valid Json String") {
        val result = dataAccess.getCountryCodes
        assert(result.nonEmpty, "country codes should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("ORIGINATING_COUNTRIES"), eventValue + " does not equal ORIGINATING_COUNTRIES")
      }
    }
  }

  describe("A mongodb query") {
    describe("to retrieve currency pairs by volume sold") {
      it("should return a valid Json String") {
        val result = dataAccess.getCurrencyPairsByVolume
        assert(result.nonEmpty, "currency pairs by volume sold should not be empty")
        // parse the String to confirm that the Json is valid
        val parsedJson = parse(result)
        val eventValue = (parsedJson \ "event").extract[String]
        assert(eventValue.equals("CURRENCY_PAIRS"), eventValue + " does not equal CURRENCY_PAIRS")
        val strValue = {
          val xs = for { JString(x) <- (parsedJson \ "data" \ "currencyFrom") } yield x
          xs match {
            case x :: xs => x.toString
            case Nil => fail("No currencyFrom field found!")
          }
        }
        assert(strValue.nonEmpty, "parsed json value should not be empty")
        assert(strValue.length == 3, "parsed json value length should == 3")
        intercept[NumberFormatException] {
          // expected this exception as String can not be parsed to Int
          strValue.toInt
        }
        val numberValue = {
          val xs = for { JInt(x) <- (parsedJson \ "data" \ "volume") } yield x
          xs match {
            case x :: xs => x.toString.toInt // will fail if not a number
            case Nil => fail("No volume field found!")
          }
        }
      }
    }
  }

}
