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

import java.math.RoundingMode
import currencytrade.api.{ MainActors, CurrencyTradeApi }
import akka.actor.{ ActorSystem, PoisonPill }
import akka.io.IO
import spray.can.Http
import spray.can.server.UHttp
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoClientURI

import scala.util.Properties

/**
 * Starts the CurrencyTrade application by spinning up the
 * currencytrade-system [[akka.actor.ActorSystem]] and binding
 * to the UHttp and Http protocol for http and web socket requests.
 *
 * This object is based on the SprayEasterEggs project.
 * Please view this project for more details.
 *
 * @see https://github.com/cuali/SprayEasterEggs
 */
object CurrencyTradeSystem extends App with MainActors with CurrencyTradeApi {
  implicit lazy val system = ActorSystem("currencytrade-system")
  sys.addShutdownHook({ system.shutdown })
  //IO(UHttp) ! Http.Bind(wsService, Configuration.host, Configuration.portWs)
  // Since the UHttp extension extends from Http extension,
  // it starts an actor whose name will later collide with the Http extension.
  //system.actorSelection("/user/IO-HTTP") ! PoisonPill
  // We could use IO(UHttp) here instead of killing the "/user/IO-HTTP" actor
  IO(Http) ! Http.Bind(rootService, Configuration.host, Configuration.portHttp)
}

/**
 * Loads application configuration settings from application.conf and
 * provides global constants.
 */
object Configuration {
  import com.typesafe.config.ConfigFactory
  import scala.util.Properties

  // load configuration settings from application.conf
  // in the default location
  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  /**
   * The ip address from which the spray routes will be served.
   * Obtained using the currencytrade.host from application.conf.
   */
  lazy val host = config.getString("currencytrade.host")

  /**
   * The port number that http requests will be served on.
   * As this app is intended to be deployed to Heroku, we first try to
   * use the PORT variable defined in Heroku. If that variable is not available
   * then the port number is obtained using the currencytrade.ports.http from application.conf.
   */
  lazy val portHttp = Properties.envOrElse("PORT", config.getString("currencytrade.ports.http")).toInt

  /**
   * The port number that web socket requests will be served on.
   * Obtained using the currencytrade.ports.ws from application.conf.
   */
  lazy val portWs = config.getInt("currencytrade.ports.ws")

  /**
   * The size of the [[java.util.concurrent.Executors]] thread pool
   * Obtained using the currencytrade.threadpool from application.conf.
   */
  lazy val threadPool = config.getInt("currencytrade.threadpool")

  /**
   * The endpoint for the trade persist url called from [[currencytrade.mock.ClientMockService]].
   * Obtained using the api.mocktrade from application.conf.
   */
  lazy val apiMockTrade = config.getString("api.mocktrade")

  /**
   * The maximum number of trade persist requests to be mocked.
   * Obtained using the api.mockmax from application.conf.
   */
  lazy val apiMockMax = config.getInt("api.mockmax")

  /**
   * The host name of the MongoDB server.
   * Obtained using the mongodb.host from application.conf.
   */
  lazy val dbHost = config.getString("mongodb.host")

  /**
   * The port number of the MongoDB server.
   * Obtained using the mongodb.port from application.conf.
   */
  lazy val dbPort = config.getInt("mongodb.port")

  /**
   * The MongoDB server database name.
   * Obtained using the mongodb.database from application.conf.
   */
  lazy val dbDatabase = config.getString("mongodb.database")

  /**
   * The MongoDB server collection name.
   * Obtained using the mongodb.collection from application.conf.
   */
  lazy val dbCollection = config.getString("mongodb.collection")

  /**
   * The [[org.joda.time.format.DateTimeFormat]] pattern that standardizes the date
   * representations used throughout the application. Obtained using the mongodb.collection
   * from application.conf.
   */
  lazy val formatter: org.joda.time.format.DateTimeFormatter = org.joda.time.format.DateTimeFormat.forPattern("dd-MMM-yy HH:mm:ss")

  /**
   * The [[java.math.BigDecimal]] rounding strategy
   */
  lazy val ROUNDING_STRATEGY = RoundingMode.UP

  /**
   * Constant of 2, used when specifying [[java.math.BigDecimal]] decimal places
   */
  lazy val DECIMAL_PLACES_2 = 2

  /**
   * Constant of 4, used when specifying [[java.math.BigDecimal]] decimal places
   */
  lazy val DECIMAL_PLACES_4 = 4
}

/**
 * Factory that provides a [[com.mongodb.casbah.MongoClient]] connection to MongoDB.
 *
 * Currencytrade is intended to be deployed to Heroku. Heroku uses a system environment
 * variable; MONGOLAB_URI, to define the MongoDB connection details. If MONGOLAB_URI is
 * defined then the value of this variable will be used otherwise the MongoDB connection
 * details defined in application.conf are used.
 */
object MongoFactory {

  /**
   * The MongoDB connection.
   * If the system environment variable MONGOLAB_URI is defined, it will be used to connect
   * to MongoDB otherwise the MongoDB connection details defined in application.conf are used.
   *
   * The format of the MONGOLAB_URI is: mongodb://dbuser:dbpass@host:port/dbname
   */
  val dbUri = Properties.envOrNone("MONGOLAB_URI")
  val collection = dbUri match {
    case Some(uri) => {
      val clientUri = MongoClientURI(uri)
      val connection = MongoClient(MongoClientURI(uri))
      connection(clientUri.database.getOrElse("heroku_ltcnz9nk"))(clientUri.collection.getOrElse(Configuration.dbCollection))
    }
    case None => {
      val connection = MongoClient(Configuration.dbHost, Configuration.dbPort)
      connection(Configuration.dbDatabase)(Configuration.dbCollection)
    }
  }
}
