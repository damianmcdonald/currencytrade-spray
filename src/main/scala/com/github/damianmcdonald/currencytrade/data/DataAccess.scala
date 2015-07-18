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

import akka.event.slf4j.SLF4JLogging
import com.github.damianmcdonald.currencytrade.MongoFactory
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers

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
  def getCountriesByTradeVolume(): String
  def getCurrenciesSoldByVolume(): String
  def getCurrenciesBoughtByVolume(): String
  def getCurrenciesSoldByValue(): String
  def getCurrenciesBoughtByValue(): String
  def getCurrencyPairsByVolume(): String
  def getLatestTrades(): String
  def getCountryCodes(): String
  val Commands: AnyRef
  val Options: AnyRef
  val Accessors: AnyRef

  /**
   * The constant definitions for Json event types
   */
  val JsonEvent = new {
    val CurrenciesSoldVolume = "CURRENCIES_SOLD_VOLUME"
    val CurrenciesSoldValue = "CURRENCIES_SOLD_VALUE"
    val CurrenciesBoughtVolume = "CURRENCIES_BOUGHT_VOLUME"
    val CurrenciesBoughtValue = "CURRENCIES_BOUGHT_VALUE"
    val CountriesVolume = "COUNTRIES_VOLUME"
    val CurrencyPairs = "CURRENCY_PAIRS"
    val LatestTrades = "LATEST_TRADES"
    val Countries = "ORIGINATING_COUNTRIES"
  }
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
   * Persists a Trade
   *
   * @param dbObject the [[com.mongodb.casbah.commons.MongoDBObject]] representation of a Trade
   * @return String the MongoDB object id of the persisted Trade
   */
  def persistTrade(dbObject: MongoDBObject): String = {
    MongoFactory.collection.save(dbObject)
    dbObject.get(Accessors.Id) match {
      case Some(id) => id.toString
      case None => "id not found"
    }
  }

  /**
   * Retrieves a JSON representation of the top 10 countries by trade volume.
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
   * ==Produces JSON==
   *
   * {
   *   "event":"COUNTRIES_VOLUME",
   *   "data":[
   *     {
   *       "volume" : 123 ,
   *       "country" : "CH"
   *     },{
   *       "volume" : 110 ,
   *       "country" : "SG"
   *     }
   *   ]
   * }
   *
   * @return String the top 10 countries by trade volume as JSON
   */
  def getCountriesByTradeVolume: String = {
    val xs = MongoFactory.collection.aggregate(
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
    listToJson(JsonEvent.CountriesVolume, xs)
  }

  /**
   * Retrieves a JSON representation of the top 10 currencies sold by volume.
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
   * ==Produces JSON==
   *
   * {
   *   "event":"CURRENCIES_SOLD_VOLUME",
   *   "data":[
   *     {
   *       "volume" : 234 ,
   *       "currency" : "EUR"
   *     },{
   *       "volume" : 212 ,
   *       "currency" : "GBP"
   *     }
   *   ]
   * }
   *
   * @return String the top 10 currencies sold by volume as JSON
   */
  def getCurrenciesSoldByVolume: String = {
    val xs = MongoFactory.collection.aggregate(
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
    listToJson(JsonEvent.CurrenciesSoldVolume, xs)
  }

  /**
   * Retrieves a JSON representation of the top 10 currencies sold by value.
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
   * ==Produces JSON==
   *
   * {
   *   "event":"CURRENCIES_SOLD_VALUE",
   *   "data":[
   *     {
   *       "value" : 5270213.150899997 ,
   *       "currency" : "GBP"
   *     },{
   *       "value" : 4612221.757900002 ,
   *       "currency" : "EUR"
   *     }
   *   ]
   * }
   *
   * @return String the top 10 currencies sold by value as JSON
   */
  def getCurrenciesSoldByValue: String = {
    val xs = MongoFactory.collection.aggregate(
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
    listToJson(JsonEvent.CurrenciesSoldValue, xs)
  }

  /**
   * Retrieves a JSON representation containing the top 10 currencies bought by volume.
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
   * ==Produces JSON==
   *
   * {
   *   "event":"CURRENCIES_BOUGHT_VOLUME",
   *   "data":[
   *     {
   *       "volume" : 177 ,
   *       "currency" : "CAD"
   *     },{
   *       "volume" : 152 ,
   *       "currency" : "USD"
   *     }
   *   ]
   * }
   *
   * @return String the top 10 currencies bought by volume as JSON
   */
  def getCurrenciesBoughtByVolume: String = {
    val xs = MongoFactory.collection.aggregate(
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
    listToJson(JsonEvent.CurrenciesBoughtVolume, xs)
  }

  /**
   * Retrieves a JSON representation containing the top 10 currencies bought by value.
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
   * ==Produces JSON==
   *
   * {
   *   "event":"CURRENCIES_BOUGHT_VALUE",
   *   "data":[
   *     {
   *       "value" : 5426183.810499998 ,
   *       "currency" : "CAD"
   *     },{
   *       "value" : 4401797.9120000005 ,
   *       "currency" : "MXP"
   *     }
   *   ]
   * }
   *
   * @return String the top 10 currencies bought by value as JSON
   */
  def getCurrenciesBoughtByValue: String = {
    val xs = MongoFactory.collection.aggregate(
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
    listToJson(JsonEvent.CurrenciesBoughtValue, xs)
  }

  /**
   * Retrieves a JSON representation containing the top 10 currencies pairs by volume sold.
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
   * ==Produces JSON==
   *
   * {
   *   "event":"CURRENCY_PAIRS",
   *   "data":[
   *     {
   *       "volume" : 27 ,
   *       "currencyFrom" : "EUR" ,
   *       "currencyTo" : "RUB"
   *     },{
   *       "volume" : 26 ,
   *       "currencyFrom" : "GBP" ,
   *       "currencyTo" : "ZAR"
   *     }
   *   ]
   * }
   *
   * @return String the top 10 currencies pairs by volume sold as JSON
   */
  def getCurrencyPairsByVolume: String = {
    val xs = MongoFactory.collection.aggregate(
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
    listToJson(JsonEvent.CurrencyPairs, xs)
  }

  /**
   * Finds the latest 10 trades
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
   * ==Produces JSON==
   *
   * {
   *   "event":"LATEST_TRADES",
   *   "data":[
   *     {
   *       "currencyFrom" : "INR" ,
   *       "currencyTo" : "GBP" ,
   *       "amountSell" : 8109.22 ,
   *       "amountBuy" : 7582.94 ,
   *       "rate" : 0.9351 ,
   *       "timePlaced" :
   *         {
   *           "$date" : "2014-07-31T10:54:52.000Z"
   *         } ,
   *       "originatingCountry" : "AE" ,
   *       "receptionDate" :
   *         {
   *           "$date" : "2015-07-13T10:54:52.105Z"
   *         }
   *     }
   *   ]
   * }
   *
   * @return String the latest 10 trades as JSON
   */
  def getLatestTrades: String = {
    val xs = MongoFactory.collection.find(
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
    listToJson(JsonEvent.LatestTrades, xs)
  }

  /**
   * Retrieves the ISO 3166-1 alpha-2, 2 letter country codes of countries from which a trade has been placed.
   *
   * See https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
   *
   * ==Corresponds to the following MongoDB query==
   *
   *  db.trades.distinct("originatingCountry")
   *
   * ==Produces JSON==
   *
   * {
   *   "event":"ORIGINATING_COUNTRIES",
   *   "data":[
   *     "AE",
   *     "AR",
   *     "AU",
   *     "BR",
   *     "CA"
   *   ]
   * }
   *
   * @return String the countries codes as JSON
   */
  def getCountryCodes: String = {
    val xs = MongoFactory.collection.distinct(Fields.OriginatingCountry).map(e => s""""${e}"""").sortWith(_ < _).toList
    listToJson(JsonEvent.Countries, xs)
  }

  /**
   * Produces a JSON representation from MongoDB queries
   *
   * @param event the name of the JSON event
   * @param xs the list of MongoDB data
   * @return String the JSON representation of the MongoDB data with event type
   */
  private def listToJson(event: String, xs: List[_]) = {
    val data = xs.mkString(",")
    s"""{"event":"$event", "data":[$data]}"""
  }

}
