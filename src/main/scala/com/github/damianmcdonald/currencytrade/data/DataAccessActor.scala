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

import akka.actor.{ Actor, ActorLogging }
import com.github.damianmcdonald.currencytrade.data.DataAccessActor._
import com.github.damianmcdonald.currencytrade.notification.NotificationActor

/**
 * Defines the messages used by the DataAccessActor class
 * to react to data access requests.
 */
object DataAccessActor {
  sealed trait DataAccessMessage
  case class CountryTradeVolume() extends DataAccessMessage
  case class CurrenciesSoldVolume() extends DataAccessMessage
  case class CurrenciesSoldValue() extends DataAccessMessage
  case class CurrenciesBoughtVolume() extends DataAccessMessage
  case class CurrenciesBoughtValue() extends DataAccessMessage
  case class CurrencyPairsVolume() extends DataAccessMessage
}

/**
 * Actor that handles requests for data access.
 */
class DataAccessActor extends Actor with ActorLogging {

  /** The data access reference */
  lazy val dataAccess: DataAccess = new MongoDBDataAccess

  /**
   * Receives messages and routes them to NotificationActor
   * via an appropriate NotificationMessage.
   */
  override def receive: PartialFunction[Any, Unit] = {
    case CountryTradeVolume =>
      context.actorSelection("/user/notifier") ! NotificationActor.JsonMessage(dataAccess.getCountriesByTradeVolume())
    case CurrenciesSoldVolume =>
      context.actorSelection("/user/notifier") ! NotificationActor.JsonMessage(dataAccess.getCurrenciesSoldByVolume())
    case CurrenciesSoldValue =>
      context.actorSelection("/user/notifier") ! NotificationActor.JsonMessage(dataAccess.getCurrenciesSoldByValue())
    case CurrenciesBoughtVolume =>
      context.actorSelection("/user/notifier") ! NotificationActor.JsonMessage(dataAccess.getCurrenciesBoughtByVolume())
    case CurrenciesBoughtValue =>
      context.actorSelection("/user/notifier") ! NotificationActor.JsonMessage(dataAccess.getCurrenciesBoughtByValue())
    case CurrencyPairsVolume =>
      context.actorSelection("/user/notifier") ! NotificationActor.JsonMessage(dataAccess.getCurrencyPairsByVolume())
    case whatever =>
      log.warning("Finding '{}'", whatever)
  }
}
