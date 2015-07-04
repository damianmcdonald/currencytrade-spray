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

package currencytrade

import scala.util.matching.Regex
import java.math.{ BigDecimal => JBigDecimal }
import currencytrade.Configuration._
import currencytrade.trade.Trade
import com.mongodb.BasicDBObject
import net.liftweb.json._

trait CurrencyTradeBase {

  /* Testing constants */
  val responseSizeExpected = 10
  private val dateTime: org.joda.time.DateTime = formatter.parseDateTime("18-Jun-15 16:19:29")

  /* String utils functions */
  private def removeControlChars(s: String) = s.filter(_ >= ' ')
  private def removeWhiteSpace(s: String) = s.replaceAll(" ", "")
  val stripString = removeControlChars _ andThen removeWhiteSpace _

  /* Compare web service response functions */
  def responseDataMatches(s: String) = {
    val resp = parse(s)
    val xs = (resp \\ "data").children.size
    xs == responseSizeExpected
  }

  def responseEventMatches(res: String, event: String) = {
    val response = parse(res)
    event.equals((response \\ "event").values.toString)
  }

  def responseIsObjectId(s: String) = {
    "^[a-f\\d]{24}$".r findFirstIn s match {
      case Some(str) => true
      case None => false
    }
  }

  /* Trade objects for tests */
  val tradeAsString = """{"userId": "bulkuser", "currencyFrom": "AUD", "currencyTo": "USD", "amountSell": 748.32, "amountBuy": 280.38, "rate": 0.7945, "timePlaced" : "18-Jun-15 16:19:29", "originatingCountry" : "AU"}"""
  val tradeAsStringMalformed = """{"userId": "bulkuser", "currencyFrom": "AUD", "currencyTo": "USD", "amountBuy": 280.38, "rate": 0.7945, "timePlaced" : "18-Jun-15 16:19:29", "originatingCountry" : "AU"}"""
  val tradeAsEntity = """{
						 |"rate": 0.7945,
						 |"currencyTo": "USD",
						 |"amountSell": 748.32,
						 |"timePlaced": "18-Jun-15 16:19:29",
						 |"amountBuy": 280.38,
						 |"originatingCountry":"AU",
						 |"userId": "bulkuser",
						 |"currencyFrom": "AUD"
						|}""".stripMargin
  val trade = Trade("bulkuser", "AUD", "USD", new JBigDecimal("748.32"), new JBigDecimal("280.38"), new JBigDecimal("0.7945"), dateTime, "AU")

  /* Countries Volume mock data and result */
  val countriesVolume = {
    val obj1, obj2, obj3 = new BasicDBObject
    obj1.put("currency", "AUD"); obj1.put("volume", 10);
    obj2.put("currency", "TRY"); obj2.put("volume", 15);
    obj3.put("currency", "EUR"); obj3.put("volume", 20);
    List(obj1, obj2, obj3)
  }
  val countriesVolumeResult = """{"event":"COUNTRIES_VOLUME","data":[{"country":"UK","volume":10},{"country":"UK","volume":15},{"country":"UK","volume":20}]}"""

  /* Currency Pairs mock data and result */
  val currencyPairs = {
    val obj1, obj2, obj3 = new BasicDBObject
    obj1.put("currencyFrom", "AUD"); obj1.put("volume", 10);
    obj2.put("currencyFrom", "TRY"); obj2.put("volume", 15);
    obj3.put("currencyFrom", "EUR"); obj3.put("volume", 20);
    List(obj1, obj2, obj3)
  }
  val currencyPairsResult = """{"event":"CURRENCY_PAIRS","data":[{"currencyPair":"AUD-USD","volume":10},{"currencyPair":"TRY-USD","volume":15},{"currencyPair":"EUR-USD","volume":20}]}"""

  /* Currencies Sold by Volume mock data and result */
  val currenciesSoldVolume = {
    val obj1, obj2, obj3 = new BasicDBObject
    obj1.put("currency", "AUD"); obj1.put("volume", 10);
    obj2.put("currency", "TRY"); obj2.put("volume", 15);
    obj3.put("currency", "EUR"); obj3.put("volume", 20);
    List(obj1, obj2, obj3)
  }
  val currenciesSoldVolumeResult = """{"event":"CURRENCIES_SOLD_VOLUME","data":[{"currency":"AUD","volume":10},{"currency":"TRY","volume":15},{"currency":"EUR","volume":20}]}"""

  /* Currencies Sold by Value mock data and result */
  val currenciesSoldValue = {
    val obj1, obj2, obj3 = new BasicDBObject
    obj1.put("currency", "AUD"); obj1.put("value", 4324.76);
    obj2.put("currency", "TRY"); obj2.put("value", 85764.56);
    obj3.put("currency", "EUR"); obj3.put("value", 21425.76);
    List(obj1, obj2, obj3)
  }
  val currenciesSoldValueResult = """{"event":"CURRENCIES_SOLD_VALUE","data":[{"currency":"AUD","value":5617.49},{"currency":"TRY","value":228871.74},{"currency":"EUR","value":19072.79}]}"""

  /* Currencies Bought by Volume mock data and result */
  val currenciesBoughtVolume = {
    val obj1, obj2, obj3 = new BasicDBObject
    obj1.put("currency", "AUD"); obj1.put("volume", 10);
    obj2.put("currency", "TRY"); obj2.put("volume", 15);
    obj3.put("currency", "EUR"); obj3.put("volume", 20);
    List(obj1, obj2, obj3)
  }
  val currenciesBoughtVolumeResult = """{"event":"CURRENCIES_BOUGHT_VOLUME","data":[{"currency":"AUD","volume":10},{"currency":"TRY","volume":15},{"currency":"EUR","volume":20}]}"""

  /* Currencies Bought by Value mock data and result */
  val currenciesBoughtValue = {
    val obj1, obj2, obj3 = new BasicDBObject
    obj1.put("currency", "AUD"); obj1.put("value", 4324.76);
    obj2.put("currency", "TRY"); obj2.put("value", 85764.56);
    obj3.put("currency", "EUR"); obj3.put("value", 21425.76);
    List(obj1, obj2, obj3)
  }
  val currenciesBoughtValueResult = """{"event":"CURRENCIES_BOUGHT_VALUE","data":[{"currency":"AUD","value":5617.49},{"currency":"TRY","value":228871.74},{"currency":"EUR","value":19072.79}]}"""

  /* Latest trades mock data and result */
  val latestTrades = {
    val obj1, obj2, obj3 = new BasicDBObject

    // trade 1
    obj1.put("currencyFrom", "AUD"); obj1.put("currencyTo", "USD"); obj1.put("amountSell", 4325.88);
    obj1.put("amountBuy", 2343.15); obj1.put("rate", 0.7471); obj1.put("originatingCountry", "TR");
    obj1.put("timePlaced", dateTime); obj1.put("receptionDate", 1434636129000L);

    // trade 2
    obj2.put("currencyFrom", "TRY"); obj2.put("currencyTo", "NZD"); obj2.put("amountSell", 3214.68);
    obj2.put("amountBuy", 7533.87); obj2.put("rate", 0.2579); obj2.put("originatingCountry", "AU");
    obj2.put("timePlaced", dateTime); obj2.put("receptionDate", 1434636129000L);

    // trade 3
    obj3.put("currencyFrom", "CAD"); obj3.put("currencyTo", "EUR"); obj3.put("amountSell", 168.90);
    obj3.put("amountBuy", 146.75); obj3.put("rate", 0.1278); obj3.put("originatingCountry", "NZ");
    obj3.put("timePlaced", dateTime); obj3.put("receptionDate", 1434636129000L);

    List(obj1, obj2, obj3)
  }
  val latestTradesResult = {
    val sb = new StringBuilder
    sb append """{"event":"LATEST_TRADES","""
    sb append """"data":[{"rate":0.7471,"currencyTo":"USD","amountSell":4325.89,"timePlaced":"18-Jun-15 16:19:29","amountBuy":2343.16,"originatingCountry":"TR","receptionDate":1434636129000,"currencyFrom":"AUD"},"""
    sb append """{"rate":0.2580,"currencyTo":"NZD","amountSell":3214.68,"timePlaced":"18-Jun-15 16:19:29","amountBuy":7533.87,"originatingCountry":"AU","receptionDate":1434636129000,"currencyFrom":"TRY"},"""
    sb append """{"rate":0.1278,"currencyTo":"EUR","amountSell":168.91,"timePlaced":"18-Jun-15 16:19:29","amountBuy":146.75,"originatingCountry":"NZ","receptionDate":1434636129000,"currencyFrom":"CAD"}]}"""
    sb toString
  }

  /* Trade persisted result */
  val tradePersistedResult = """{"event":"TRADE_PERSISTED","data":{"rate":0.7945,"currencyTo":"USD","amountSell":748.32,"timePlaced":"18-Jun-1516:19:29","amountBuy":280.38,"originatingCountry":"AU","currencyFrom":"AUD"}}"""

  /* Country codes mock data and result */
  val countryCodes = List("AU", "TR", "ES")
  val countryCodeResults = """{"event":"ORIGINATING_COUNTRIES","data":[{"country":"AU"},{"country":"TR"},{"country":"ES"}]}"""

}
