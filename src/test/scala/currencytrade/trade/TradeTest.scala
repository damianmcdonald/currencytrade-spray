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

import org.specs2.mutable.Specification
import com.mongodb.casbah.commons.MongoDBObject
import currencytrade.CurrencyTradeBase
import currencytrade.trade.TradeJsonProtocol._
import spray.http.HttpEntity
import spray.httpx.SprayJsonSupport
import spray.http.ContentTypes
import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._

class TradeTest extends Specification with SprayJsonSupport with CurrencyTradeBase {

  def marshaller: Marshaller[Trade] = sprayJsonMarshaller[Trade]
  def unmarshaller: Unmarshaller[Trade] = sprayJsonUnmarshaller[Trade]

  "A Trade" should {
    "when ready for persistence to MongoDB" should {
      "produce a valid MongoDBObject" in {
        val result = Trade.buildMongoDbObjectFromTrade(trade)
        result.isInstanceOf[MongoDBObject]
        result.containsField("userId")
        result.containsField("currencyFrom")
        result.containsField("currencyTo")
        result.containsField("amountSell")
        result.containsField("amountBuy")
        result.containsField("rate")
        result.containsField("originatingCountry")
        result.containsField("timePlaced")
        result.containsField("receptionDate")
      }
    }
  }

  "A Trade" should {
    "when marshalled" should {
      "produce a valid json representation of the the trade" in {
        val entity = stripString(marshal(trade).right.get.asString)
        entity.equals(stripString(tradeAsEntity))
      }
    }
  }

  "A Trade" should {
    "when unmarshalled" should {
      "produce a valid trade object" in {
        HttpEntity(ContentTypes.`application/json`, tradeAsString).as[Trade] === Right(trade)
      }
    }
  }

}
