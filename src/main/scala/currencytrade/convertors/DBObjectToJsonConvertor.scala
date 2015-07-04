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

import com.mongodb.casbah.Imports._
import java.math.{ BigDecimal => JBigDecimal }
import currencytrade.Configuration._
import currencytrade.notification.NotificationActor._
import currencytrade.xrates.OpenExchangeRates
import currencytrade.trade.Trade
import java.util.Date
import org.joda.time.DateTime
import currencytrade.data.DataAccessConstants
import spray.json._
import spray.json.DefaultJsonProtocol._

/**
 * Defines a transform function.
 *
 * ==Usage==
 *
 * private def transform[T1, T2](x: T1, y: T2)(implicit transformer: Transformer[T1, T2]): T2 = transformer.transform(x, y)
 *
 * private implicit object ExampleTransfomer extends Transformer[String, String] {
 *     def transform(x: String, y: String): String = {
 *         x + y
 *     }
 * }
 *
 */
abstract class Transformer[T1, T2] { def transform(x: T1, y: T2): T2 }

/**
 * Convertor that provides functionalities to convert from [[com.mongodb.casbah.commons.MongoDBObject]]
 * to Json ([[java.lang.String]]) representation
 */
object DBObjectToJsonConvertor extends DataAccessConstants {

  /**
   * The constant definitions for Json event types
   */
  private val JsonEvent = new {
    val CurrenciesSoldVolume = "CURRENCIES_SOLD_VOLUME"
    val CurrenciesSoldValue = "CURRENCIES_SOLD_VALUE"
    val CurrenciesBoughtVolume = "CURRENCIES_BOUGHT_VOLUME"
    val CurrenciesBoughtValue = "CURRENCIES_BOUGHT_VALUE"
    val CountriesVolume = "COUNTRIES_VOLUME"
    val CurrencyPairs = "CURRENCY_PAIRS"
    val LatestTrades = "LATEST_TRADES"
    val TradePersisted = "TRADE_PERSISTED"
    val Countries = "ORIGINATING_COUNTRIES"
  }

  /**
   * The constant definitions for Json default data values
   */
  private val Defaults = new {
    val Currency = "USD"
    val Volume = 0
    val Value = 0.0
    val DateTime = new DateTime
    val TimeStamp = new Date().getTime
    val Country = "UK"
    val ConcatChar = "-"
  }

  /**
   * Converts a [[currencytrade.notification.NotificationMessage]] into an appropriate
   * Json ([[java.lang.String]]) representation
   *
   * @param msg the [[currencytrade.notification.NotificationMessage]] containing the data to be represented in Json
   * @return String the Json ([[java.lang.String]]) representation
   */
  def convert(msg: NotificationMessage): String = {
    msg match {
      case TradePersisted(t) => tradePersistedJson(t).toJson.compactPrint
      case CurrenciesSoldByVolume(xs) => currenciesSoldByVolumeJson(xs).toJson.compactPrint
      case CurrenciesBoughtByVolume(xs) => currenciesBoughtByVolumeJson(xs).toJson.compactPrint
      case CurrenciesSoldByValue(xs) => currenciesSoldByValueJson(xs).toJson.compactPrint
      case CurrenciesBoughtByValue(xs) => currenciesBoughtByValueJson(xs).toJson.compactPrint
      case TradeVolumeByCountry(xs) => countryVolumeJson(xs).toJson.compactPrint
      case CurrencyPairsByVolume(xs) => currencyPairsByVolumeJson(xs).toJson.compactPrint
      case _ => throw new IllegalArgumentException("NotificationMessage message type not found")
    }
  }

  /**
   * Converts the latest 10 trades data into an appropriate Json ([[java.lang.String]]) representation
   *
   * ==Json format example==
   *
   * {
   *  "event":"LATEST_TRADES",
   *  "data":[
   *   {   "rate":0.7471,
   *        "currencyTo":"USD",
   *         "amountSell":4325.89,
   *         "timePlaced":"18-Jun-15 16:19:29",
   *         "amountBuy":2343.16,
   *         "originatingCountry":"TR",
   *         "receptionDate":1434636129000,
   *         "currencyFrom":"AUD"
   *    },
   *    {    "rate":0.2580,
   *         "currencyTo":"NZD",
   *         "amountSell":3214.68,
   *         "timePlaced":"18-Jun-15 16:19:29",
   *         "amountBuy":7533.87,
   *         "originatingCountry":"AU",
   *         "receptionDate":1434636129000,
   *         "currencyFrom":"TRY"
   *    }
   *  ]
   * }
   *
   * @param xs the [[scala.collection.immutable.List]] containing the latest 10 trades data
   * @return String the Json ([[java.lang.String]]) representation
   */
  def latestTadesJson(xs: List[DBObject]): String = {
    val data = JsArray(xs.map(x => JsObject(
      Fields.CurrencyFrom -> JsString(x.getAsOrElse[String](Fields.CurrencyFrom, Defaults.Currency)),
      Fields.CurrencyTo -> JsString(x.getAsOrElse[String](Fields.CurrencyTo, Defaults.Currency)),
      Fields.AmountSell -> JsNumber(new JBigDecimal(x.getAsOrElse[Double](Fields.AmountSell, Defaults.Value)).setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)),
      Fields.AmountBuy -> JsNumber(new JBigDecimal(x.getAsOrElse[Double](Fields.AmountBuy, Defaults.Value)).setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)),
      Fields.Rate -> JsNumber(new JBigDecimal(x.getAsOrElse[Double](Fields.Rate, Defaults.Value)).setScale(DECIMAL_PLACES_4, ROUNDING_STRATEGY)),
      Fields.OriginatingCountry -> JsString(x.getAsOrElse[String](Fields.OriginatingCountry, Defaults.Country)),
      Fields.TimePlaced -> JsString(formatter.print(x.getAsOrElse[DateTime](Fields.TimePlaced, Defaults.DateTime))),
      Fields.ReceptionDate -> JsNumber(x.getAsOrElse[Long](Fields.ReceptionDate, Defaults.TimeStamp))
    )).toVector)
    toJsObject(JsonEvent.LatestTrades, data).toJson.compactPrint
  }

  /**
   * Converts country codes data into an appropriate Json ([[java.lang.String]]) representation
   *
   * ==Json format example==
   *
   * {
   *   "event":"ORIGINATING_COUNTRIES",
   *   "data":[
   *       {"country":"AU"},
   *       {"country":"TR"},
   *       {"country":"ES"}
   *   ]
   * }
   *
   * @param xs the [[scala.collection.immutable.List]] containing the country codes
   * @return String the Json ([[java.lang.String]]) representation
   */
  def countryCodesJson(xs: List[String]): String = {
    val data = JsArray(xs.map(x => JsObject(Fields.Country -> JsString(x))).toVector)
    toJsObject(JsonEvent.Countries, data).toJson.compactPrint
  }

  /**
   * Generic transform function
   */
  private def transform[T1, T2](x: T1, y: T2)(implicit transformer: Transformer[T1, T2]): T2 = transformer.transform(x, y)

  /**
   * Transformer that converts a currency value to a base currency value
   */
  private implicit object BaseRateTransfomer extends Transformer[String, JBigDecimal] {
    /**
     * Transforms a currency value to a new value using a base currency for conversion
     *
     * @param x the [[java.lang.String]] representing the 2 letter currency code to convert
     * @param y the [[java.math.BigDecimal]] representing the value to convert to the base currency
     * @return BigDecimal the currency value converted to a base currency value
     */
    def transform(x: String, y: JBigDecimal): JBigDecimal = {
      val rates = new OpenExchangeRates()
      rates.convertToBaseCurrency(x, y).getOrElse(new JBigDecimal(Defaults.Value))
    }
  }

  /**
   * Creates a [[spray.json.JsObject]] response
   *
   * @param event the Json event
   * @param data the Json data array
   * @return JsObject the Json representation
   */
  private def toJsObject(event: String, data: JsArray) = {
    JsObject(Fields.Event -> JsString(event), Fields.Data -> data)
  }

  /**
   * Creates a [[spray.json.JsObject]] response
   *
   * @param event the Json event
   * @param data the Json data object
   * @return JsObject the Json representation
   */
  private def toJsObject(event: String, data: JsObject) = {
    JsObject(Fields.Event -> JsString(event), Fields.Data -> data)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for persisted trade data
   *
   * ==Json format example==
   *
   * {
   *   "event":"TRADE_PERSISTED",
   *   "data":{
   *     "rate":0.7945,
   *     "currencyTo":"USD",
   *     "amountSell":748.32,
   *     "timePlaced":"18-Jun-1516:19:29",
   *     "amountBuy":280.38,
   *     "originatingCountry":"AU",
   *     "currencyFrom":"AUD"
   *   }
   * }
   *
   * @param t the persisted trade be represented as Json
   * @return JsObject the Json representation
   */
  private def tradePersistedJson = (t: Trade) => {
    val data = JsObject(
      Fields.CurrencyFrom -> JsString(t.currencyFrom),
      Fields.CurrencyTo -> JsString(t.currencyTo),
      Fields.AmountSell -> JsNumber(t.amountSell.setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)),
      Fields.AmountBuy -> JsNumber(t.amountBuy.setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)),
      Fields.Rate -> JsNumber(t.rate.setScale(DECIMAL_PLACES_4, ROUNDING_STRATEGY)),
      Fields.OriginatingCountry -> JsString(t.originatingCountry),
      Fields.TimePlaced -> JsString(formatter.print(t.timePlaced))
    )
    toJsObject(JsonEvent.TradePersisted, data)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for currency pairs by volume data
   *
   * ==Json format example==
   *
   * {
   *  "event":"CURRENCY_PAIRS",
   *  "data":[
   *    {"currencyPair":"AUD-USD","volume":10},
   *    {"currencyPair":"TRY-USD","volume":15},
   *    {"currencyPair":"EUR-USD","volume":20}
   *  ]
   * }
   *
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def currencyPairsByVolumeJson = (xs: List[DBObject]) => {
    def concat(s: String*) = s filter (_.nonEmpty) mkString Defaults.ConcatChar
    val data = JsArray(xs.map(x => JsObject(
      Fields.CurrencyPair -> JsString(
        concat(
          x.getAsOrElse[String](Fields.CurrencyFrom, Defaults.Currency),
          x.getAsOrElse[String](Fields.CurrencyTo, Defaults.Currency)
        )
      ),
      Fields.Volume -> JsNumber(x.getAsOrElse[Int](Fields.Volume, Defaults.Volume))
    )).toVector)
    toJsObject(JsonEvent.CurrencyPairs, data)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for the top countries by trade volume data
   *
   * ==Json format example==
   *
   * {
   *   "event":"COUNTRIES_VOLUME",
   *   "data":[
   *     {"country":"UK","volume":10},
   *     {"country":"UK","volume":15},
   *     {"country":"UK","volume":20}
   *   ]
   * }
   *
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def countryVolumeJson = (xs: List[DBObject]) => {
    val data = JsArray(xs.map(x => JsObject(
      Fields.Country -> JsString(x.getAsOrElse[String](Fields.Country, Defaults.Country)),
      Fields.Volume -> JsNumber(x.getAsOrElse[Int](Fields.Volume, Defaults.Volume))
    )).toVector)
    toJsObject(JsonEvent.CountriesVolume, data)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for currency volume data
   *
   * @param event the Json event
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def currenciesByVolumeJson = (event: String, xs: List[DBObject]) => {
    val data = JsArray(xs.map(x => JsObject(
      Fields.Currency -> JsString(x.getAsOrElse[String](Fields.Currency, Defaults.Currency)),
      Fields.Volume -> JsNumber(x.getAsOrElse[Int](Fields.Volume, Defaults.Volume))
    )).toVector)
    toJsObject(event, data)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for currency value data
   *
   * @param event the Json event
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def currenciesByValueJson = (event: String, xs: List[DBObject]) => {
    val data = JsArray(xs.map(x => JsObject(
      Fields.Currency -> JsString(x.getAsOrElse[String](Fields.Currency, Defaults.Currency)),
      Fields.Value -> JsNumber(
        transform(
          x.getAsOrElse[String](Fields.Currency, Defaults.Currency),
          new JBigDecimal(x.getAsOrElse[Double](Fields.Value, Defaults.Value))
        )
      )
    )).toVector)
    toJsObject(event, data)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for currencies sold by value data
   *
   * ==Json format example==
   *
   * {
   *   "event":"CURRENCIES_SOLD_VALUE",
   *   "data":[
   *      {"currency":"AUD","value":5423.76},
   *      {"currency":"TRY","value":1285.08},
   *      {"currency":"EUR","value":958.42}
   *   ]
   * }
   *
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def currenciesSoldByValueJson = (xs: List[DBObject]) => {
    currenciesByValueJson(JsonEvent.CurrenciesSoldValue, xs)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for currencies bought by value data
   *
   * ==Json format example==
   *
   * {
   *   "event":"CURRENCIES_BOUGHT_VALUE",
   *   "data":[
   *     {"currency":"AUD","value":5423.76},
   *     {"currency":"TRY","value":1285.08},
   *     {"currency":"EUR","value":958.42}
   *   ]
   * }
   *
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def currenciesBoughtByValueJson = (xs: List[DBObject]) => {
    currenciesByValueJson(JsonEvent.CurrenciesBoughtValue, xs)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for currencies sold by volume data
   *
   * ==Json format example==
   *
   * {
   *   "event":"CURRENCIES_SOLD_VOLUME",
   *   "data":[
   *     {"currency":"AUD","volume":10},
   *     {"currency":"TRY","volume":15},
   *     {"currency":"EUR","volume":20}
   *   ]
   * }
   *
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def currenciesSoldByVolumeJson = (xs: List[DBObject]) => {
    currenciesByVolumeJson(JsonEvent.CurrenciesSoldVolume, xs)
  }

  /**
   * Creates a [[spray.json.JsObject]] response for currencies bought by volume data
   *
   * ==Json format example==
   *
   * {
   *   "event":"CURRENCIES_BOUGHT_VOLUME",
   *   "data":[
   *     {"currency":"AUD","volume":10},
   *     {"currency":"TRY","volume":15},
   *     {"currency":"EUR","volume":20}
   *   ]
   * }
   *
   * @param data the data to be represented as Json
   * @return JsObject the Json representation
   */
  private def currenciesBoughtByVolumeJson = (xs: List[DBObject]) => {
    currenciesByVolumeJson(JsonEvent.CurrenciesBoughtVolume, xs)
  }

}
