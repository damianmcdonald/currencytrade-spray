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

package com.github.damianmcdonald.currencytrade.trade

import java.math.{ BigDecimal => JBigDecimal }
import java.util.Date

import com.github.damianmcdonald.currencytrade.Configuration._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import spray.json._

/**
 * A currency trade. Companion object to the Trade case class.
 */
object Trade {
  /**
   * Creates a [[com.mongodb.casbah.commons.MongoDBObject]] representation of a Trade
   *
   * @param trade the Trade
   * @return MongoDBObject the [[com.mongodb.casbah.commons.MongoDBObject]] representation or a Trade
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
   * Pretty prints the attributes of a Trade
   *
   * @return String a pretty print representation of the attributes of Trade
   */
  override def toString: String = {
    "userId: " + userId +
      ", currencyFrom: " + currencyFrom +
      ", currencyTo: " + currencyTo +
      ", amountSell: " + amountSell +
      ", amountBuy: " + amountBuy +
      ", rate: " + rate +
      ", timePlaced: " + formatter.print(timePlaced) +
      ", originatingCountry: " + originatingCountry
  }

  /**
   * Converts a Trade to JSON representation
   *
   * @return String a JSON representation of Trade
   */
  def toJson: String = {
    s"""{
  |"event":"TRADE_PERSISTED",
  |"data":{
  |"userId":"$userId",
  |"currencyFrom":"$currencyFrom",
  |"currencyTo":"$currencyTo",
  |"amountSell":$amountSell,
  |"amountBuy":$amountBuy,
  |"rate":$rate,
  |"timePlaced":"${formatter.print(timePlaced)}",
  |"originatingCountry":"$originatingCountry"
  |}
  |}""".stripMargin.filter(_ >= ' ')
  }
}

/**
 * Json marshaller and unmarshaller for Trade
 */
object TradeJsonProtocol extends DefaultJsonProtocol {

  /**
   * Provide marshalling and unmarshalling for Trade
   */
  implicit object TradeJsonFormat extends RootJsonFormat[Trade] {
    /**
     * Writes/marshalls a Trade to a [[spray.json.JsObject]]
     *
     * @param trade the [[com.github.damianmcdonald.currencytrade.trade.Trade]] to marshall
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
     * Reads/unmarshalls a [[spray.json.JsValue]] to a Trade
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
