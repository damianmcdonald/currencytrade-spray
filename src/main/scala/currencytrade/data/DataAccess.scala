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

import akka.event.slf4j.SLF4JLogging
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import currencytrade.MongoFactory
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

/**
 * Trait containing constants used to ensure consistent access to BSON data items.
 */
trait DataAccessConstants {

  /**
   * The fields represented in the BSON documents stored in the database.
   */
  val Fields = new {
    val Currency = "currency"
    val Volume = "volume"
    val Event = "event"
    val Data = "data"
    val Value = "value"
    val Country = "country"
    val CurrencyPair = "currencyPair"
    val CurrencyTo = "currencyTo"
    val CurrencyFrom = "currencyFrom"
    val AmountSell = "amountSell"
    val AmountBuy = "amountBuy"
    val Rate = "rate"
    val OriginatingCountry = "originatingCountry"
    val TimePlaced = "timePlaced"
    val ReceptionDate = "receptionDate"
    val NotificationDate = "notificationDate"
  }
}

/**
 * Defines the database methods and values expected by the currencytrade application.
 */
trait DataAccess {
  def persistTrade(dbObject: MongoDBObject): String
  def getCountriesByTradeVolume(): List[DBObject]
  def getCurrenciesSoldByVolume(): List[DBObject]
  def getCurrenciesBoughtByVolume(): List[DBObject]
  def getCurrenciesSoldByValue(): List[DBObject]
  def getCurrenciesBoughtByValue(): List[DBObject]
  def getCurrencyPairsByVolume(): List[DBObject]
  def getLatestTrades(): List[DBObject]
  def getCountryCodes(): List[String]
  val Commands: AnyRef
  val Options: AnyRef
  val Accessors: AnyRef
}

/**
 * Used to access and persist MongoDB data
 */
class MongoDBDataAccess extends DataAccess with DataAccessConstants with SLF4JLogging {

  /**
   * Constants representing MongoDB commands
   */
  override val Commands = new {
    val Group = "$group"
    val Sort = "$sort"
    val Limit = "$limit"
    val Project = "$project"
    val Sum = "$sum"
  }

  /**
   * Constants representing MongoDB options
   */
  override val Options = new {
    val SortAsc = -1
    val SortDesc = 1
    val Limit = 10
    val AggregationOpts = AggregationOptions(AggregationOptions.CURSOR)
  }

  /**
   * Constants representing MongoDB accessors
   */
  override val Accessors = new {
    val Id = "_id"
    val VId = "$_id"
    val VOriginatingCountry = "$originatingCountry"
    val VCurrencyFrom = "$currencyFrom"
    val VCurrencyTo = "$currencyTo"
    val VIdCurrencyFrom = "$_id.currencyFrom"
    val VIdCurrencyTo = "$_id.currencyTo"
    val VAmountSell = "$amountSell"
    val VAmountBuy = "$amountBuy"
  }

  // register a convertor for [[org.joda.time.format.DateTimeFormat]]
  // see https://mongodb.github.io/casbah/guide/serialisation.html for more details
  RegisterJodaTimeConversionHelpers()

  /**
   * Persists a [[currencytrade.trade.Trade]]
   *
   * @param dbObject the [[com.mongodb.casbah.commons.MongoDBObject]] representation of a [[currencytrade.trade.Trade]]
   * @return String the MongoDB object id of the persisted [[currencytrade.trade.Trade]]
   */
  def persistTrade(dbObject: MongoDBObject): String = {
    MongoFactory.collection.save(dbObject)
    dbObject.get(Accessors.Id) match {
      case Some(id) => id.toString
      case None => "id not found"
    }
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the top 10 countries
   * by trade volume.
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.aggregate( [
   *    { $group : { _id : "$originatingCountry", volume: { $sum: 1 } } },
   *    { "$project":{"_id":0, "country":"$_id", "volume": 1 } },
   *    { $sort: { count: -1 } },
   *    { $limit:10 }
   *  ] )
   *
   * @return List[DBObject] the top 10 countries by trade volume
   */
  def getCountriesByTradeVolume: List[DBObject] = {
    MongoFactory.collection.aggregate(
      List(
        MongoDBObject(Commands.Group -> MongoDBObject(
          Accessors.Id -> Accessors.VOriginatingCountry,
          Fields.Volume -> MongoDBObject(Commands.Sum -> 1)
        )),
        MongoDBObject(Commands.Project ->
          MongoDBObject(Accessors.Id -> 0, Fields.Country -> Accessors.VId, Fields.Volume -> 1)),
        MongoDBObject(Commands.Sort -> MongoDBObject(Fields.Volume -> Options.SortAsc)),
        MongoDBObject(Commands.Limit -> Options.Limit)
      ), Options.AggregationOpts
    ).toList
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the top 10 currencies
   * sold by volume.
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.aggregate( [
   *    { $group : { _id : "$currencyFrom", volume: { $sum: 1 } } },
   *    { "$project":{"_id":0, "currency":"$_id", "volume": 1 } },
   *    { $sort: { volume: -1 } },
   *    { $limit:10 }
   *  ] )
   *
   * @return List[DBObject] the top 10 currencies sold by volume
   */
  def getCurrenciesSoldByVolume: List[DBObject] = {
    MongoFactory.collection.aggregate(
      List(
        MongoDBObject(Commands.Group -> MongoDBObject(
          Accessors.Id -> Accessors.VCurrencyFrom,
          Fields.Volume -> MongoDBObject(Commands.Sum -> 1)
        )),
        MongoDBObject(Commands.Project ->
          MongoDBObject(Accessors.Id -> 0, Fields.Currency -> Accessors.VId, Fields.Volume -> 1)),
        MongoDBObject(Commands.Sort -> MongoDBObject(Fields.Volume -> Options.SortAsc)),
        MongoDBObject(Commands.Limit -> Options.Limit)
      ), Options.AggregationOpts
    ).toList
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the top 10 currencies
   * sold by value.
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.aggregate( [
   *    { $group: { _id: "$currencyFrom", value: { $sum: "$amountSell" } } },
   *    { "$project":{"_id":0, "currency":"$_id", "value": 1 } },
   *    { $sort: { value: -1 } },
   *    { $limit:10 }
   *  ] )
   *
   * @return List[DBObject] the top 10 currencies sold by value
   */
  def getCurrenciesSoldByValue: List[DBObject] = {
    MongoFactory.collection.aggregate(
      List(
        MongoDBObject(Commands.Group -> MongoDBObject(
          Accessors.Id -> Accessors.VCurrencyFrom,
          Fields.Value -> MongoDBObject(Commands.Sum -> Accessors.VAmountSell)
        )),
        MongoDBObject(Commands.Project ->
          MongoDBObject(Accessors.Id -> 0, Fields.Currency -> Accessors.VId, Fields.Value -> 1)),
        MongoDBObject(Commands.Sort -> MongoDBObject(Fields.Value -> Options.SortAsc)),
        MongoDBObject(Commands.Limit -> Options.Limit)
      ), Options.AggregationOpts
    ).toList
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the top 10 currencies
   * bought by volume.
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.aggregate( [
   *    { $group : { _id : "$currencyTo", volume: { $sum: 1 } } },
   *    { "$project":{"_id":0, "currency":"$_id", "volume": 1 } },
   *    { $sort: { volume: -1 } },
   *    { $limit:10 }
   *  ] )
   *
   * @return List[DBObject] the top 10 currencies bought by volume
   */
  def getCurrenciesBoughtByVolume: List[DBObject] = {
    MongoFactory.collection.aggregate(
      List(
        MongoDBObject(Commands.Group -> MongoDBObject(
          Accessors.Id -> Accessors.VCurrencyTo,
          Fields.Volume -> MongoDBObject(Commands.Sum -> 1)
        )),
        MongoDBObject(Commands.Project ->
          MongoDBObject(Accessors.Id -> 0, Fields.Currency -> Accessors.VId, Fields.Volume -> 1)),
        MongoDBObject(Commands.Sort -> MongoDBObject(Fields.Volume -> Options.SortAsc)),
        MongoDBObject(Commands.Limit -> Options.Limit)
      ), Options.AggregationOpts
    ).toList
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the top 10 currencies
   * bought by value.
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.aggregate( [
   *    { $group: { _id: "$currencyTo", value: { $sum: "$amountBuy" } } },
   *    { "$project":{"_id":0, "currency":"$_id", "value": 1 } },
   *    { $sort: { value: -1 } },
   *    { $limit:10 }
   *  ] )
   *
   * @return List[DBObject] the top 10 currencies bought by value
   */
  def getCurrenciesBoughtByValue: List[DBObject] = {
    MongoFactory.collection.aggregate(
      List(
        MongoDBObject(Commands.Group -> MongoDBObject(
          Accessors.Id -> Accessors.VCurrencyTo,
          Fields.Value -> MongoDBObject(Commands.Sum -> Accessors.VAmountBuy)
        )),
        MongoDBObject(Commands.Project ->
          MongoDBObject(Accessors.Id -> 0, Fields.Currency -> Accessors.VId, Fields.Value -> 1)),
        MongoDBObject(Commands.Sort -> MongoDBObject(Fields.Value -> Options.SortAsc)),
        MongoDBObject(Commands.Limit -> Options.Limit)
      ), Options.AggregationOpts
    ).toList
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the top 10 currencies
   * pairs by volume sold.
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.aggregate([
   *    { "$group": { "_id": { "currencyFrom": "$currencyFrom", "currencyTo": "$currencyTo" }, "volume": { "$sum": 1 } }},
   *    { "$project":{"_id":0, "currencyPair":"$_id.currencyFrom", "currencyTo":"$_id.currencyTo", "volume": 1 } },
   *    { $sort: { volume: -1 } },
   *    { $limit:10 }
   *  ])
   *
   * @return List[DBObject] the top 10 currencies pairs by volume sold
   */
  def getCurrencyPairsByVolume: List[DBObject] = {
    MongoFactory.collection.aggregate(
      List(
        MongoDBObject(Commands.Group -> MongoDBObject(
          Accessors.Id -> MongoDBObject(Fields.CurrencyFrom -> Accessors.VCurrencyFrom, Fields.CurrencyTo -> Accessors.VCurrencyTo),
          Fields.Volume -> MongoDBObject(Commands.Sum -> 1)
        )),
        MongoDBObject(Commands.Project ->
          MongoDBObject(Accessors.Id -> 0, Fields.CurrencyFrom -> Accessors.VIdCurrencyFrom, Fields.CurrencyTo -> Accessors.VIdCurrencyTo, Fields.Volume -> 1)),
        MongoDBObject(Commands.Sort -> MongoDBObject(Fields.Volume -> Options.SortAsc)),
        MongoDBObject(Commands.Limit -> Options.Limit)
      ), Options.AggregationOpts
    ).toList
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the latest 10 trades
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.find(
   *    null,
   *    {_id:0, currencyFrom:1, currencyTo:1, amountSell:1, amountBuy:1, rate:1, timePlaced:1 }
   *  )
   *  .sort( {receptionDate:-1} )
   *  .limit(10)
   *
   * @return List[DBObject] the latest 10 trades
   */
  def getLatestTrades: List[DBObject] = {
    MongoFactory.collection.find(
      MongoDBObject.empty,
      MongoDBObject(
        Accessors.Id -> 0,
        Fields.CurrencyFrom -> 1,
        Fields.CurrencyTo -> 1,
        Fields.AmountSell -> 1,
        Fields.AmountBuy -> 1,
        Fields.Rate -> 1,
        Fields.OriginatingCountry -> 1,
        Fields.TimePlaced -> 1,
        Fields.ReceptionDate -> 1
      )
    )
      .sort(MongoDBObject(Fields.ReceptionDate -> Options.SortAsc))
      .limit(Options.Limit)
      .toList
  }

  /**
   * Retrieves a [[scala.collection.immutable.List]] containing the ISO 3166-1 alpha-2, 2 letter country codes
   * of countries from which a trade has been placed.
   *
   * See https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.distinct("originatingCountry")
   *
   * @return List[String] the countries codes
   */
  def getCountryCodes: List[String] = {
    MongoFactory.collection.distinct(Fields.OriginatingCountry).map(e => e.toString).sortWith(_ < _).toList
  }

}
