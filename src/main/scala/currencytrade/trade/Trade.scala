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

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import spray.json._
import java.util.Date
import currencytrade.Configuration._
import java.math.{ BigDecimal => JBigDecimal }

/**
 * A currency trade. Companion object to the [[currencytrade.trade.Trade]] case class.
 */
object Trade {
  /**
   * Creates a [[com.mongodb.casbah.commons.MongoDBObject]] representation of a [[currencytrade.trade.Trade]]
   *
   * @param trade the [[currencytrade.trade.Trade]]
   * @return MongoDBObject the [[com.mongodb.casbah.commons.MongoDBObject]] representation or a [[currencytrade.trade.Trade]]
   */
  def buildMongoDbObjectFromTrade(trade: Trade): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "userId" -> trade.userId
    builder += "currencyFrom" -> trade.currencyFrom
    builder += "currencyTo" -> trade.currencyTo
    builder += "amountSell" -> trade.amountSell.doubleValue()
    builder += "amountBuy" -> trade.amountBuy.doubleValue()
    builder += "rate" -> trade.rate.doubleValue()
    builder += "timePlaced" -> trade.timePlaced
    builder += "originatingCountry" -> trade.originatingCountry
    builder += "receptionDate" -> new Date
    builder.result
  }
}

/**
 * A currency trade
 */
case class Trade(userId: String, currencyFrom: String, currencyTo: String, amountSell: JBigDecimal, amountBuy: JBigDecimal,
    rate: JBigDecimal, timePlaced: org.joda.time.DateTime, originatingCountry: String) {
  /**
   * Pretty prints the attributes of a [[currencytrade.trade.Trade]]
   *
   * @return String a pretty print representation of the attributes of [[currencytrade.trade.Trade]]
   */
  override def toString(): String = {
    "userId: " + userId +
      ", currencyFrom: " + currencyFrom +
      ", currencyTo: " + currencyTo +
      ", amountSell: " + amountSell +
      ", amountBuy: " + amountBuy +
      ", rate: " + rate +
      ", timePlaced: " + formatter.print(timePlaced) +
      ", originatingCountry: " + originatingCountry
  }
}

/**
 * Json marshaller and unmarshaller for [[currencytrade.trade.Trade]]
 */
object TradeJsonProtocol extends DefaultJsonProtocol {

  /**
   * Provide marshalling and unmarshalling for [[currencytrade.trade.Trade]]
   */
  implicit object TradeJsonFormat extends RootJsonFormat[Trade] {
    /**
     * Writes/marshalls a [[currencytrade.trade.Trade]] to a [[spray.json.JsObject]]
     *
     * @param trade the [[currencytrade.trade.Trade]] to marshall
     * @return JsObject the marhsalled representation of the Trade
     */
    def write(t: Trade): JsObject = JsObject(
      "userId" -> JsString(t.userId),
      "currencyFrom" -> JsString(t.currencyFrom),
      "currencyTo" -> JsString(t.currencyTo),
      "amountSell" -> JsNumber(t.amountSell.setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)),
      "amountBuy" -> JsNumber(t.amountBuy.setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)),
      "rate" -> JsNumber(t.rate.setScale(DECIMAL_PLACES_4, ROUNDING_STRATEGY)),
      "timePlaced" -> JsString(formatter.print(t.timePlaced)),
      "originatingCountry" -> JsString(t.originatingCountry)
    )

    /**
     * Reads/unmarshalls a [[spray.json.JsValue]] to a [[currencytrade.trade.Trade]]
     *
     * @param value the [[spray.json.JsValue]] to unmarshall
     * @return Trade the unmarhsalled representation of the JsValue
     */
    def read(value: JsValue): Trade = {
      value.asJsObject.getFields("userId", "currencyFrom", "currencyTo", "amountSell", "amountBuy", "rate", "timePlaced", "originatingCountry") match {
        case Seq(JsString(userId), JsString(currencyFrom), JsString(currencyTo), JsNumber(amountSell), JsNumber(amountBuy),
          JsNumber(rate), JsString(timePlaced), JsString(originatingCountry)) =>
          new Trade(
            userId,
            currencyFrom,
            currencyTo,
            new JBigDecimal(amountSell.toString),
            new JBigDecimal(amountBuy.toString()),
            new JBigDecimal(rate.toString()),
            formatter.parseDateTime(timePlaced),
            originatingCountry
          )
        case _ => throw new DeserializationException("Trade expected")
      }
    }
  }
}
