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

package com.github.damianmcdonald.currencytrade.mock

import java.math.{ BigDecimal => JBigDecimal }

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import com.github.damianmcdonald.currencytrade.Configuration._
import com.github.damianmcdonald.currencytrade.api.CoreAdditions
import com.github.damianmcdonald.currencytrade.trade.Trade
import com.github.damianmcdonald.currencytrade.trade.TradeJsonProtocol._
import com.github.damianmcdonald.currencytrade.xrates.OpenExchangeRates
import spray.client.pipelining._
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.SprayJsonSupport._

import scala.concurrent._
import scala.util.{ Failure, Random, Success }

/**
 * Represents the country and currency data used in mock trades
 */
case class MockCountryData(countryCode: String, currencyCode: String)

/**
 * API routes that handle requests to place mock trades
 */
class ClientMockService(implicit system: ActorSystem) extends CoreAdditions with SLF4JLogging {

  /**
   * API routes served by this class
   */
  lazy val route =
    pathPrefix("v1") {
      path("mocktrade") {
        post {
          respondWithMediaType(`application/json`) {
            onSuccess(Future {
              sendRandomPostData
            }) {
              result => complete(result.toJson)
            }
          }
        }
      } ~
        path("bulktrades") {
          post {
            respondWithMediaType(`text/plain`) {
              onSuccess(Future {
                val mock = getMockTrade()
                sendBulkPostData
              }) {
                result => { complete(result.toString) }
              }
            }
          }
        }
    }

  /**
   * Default value ranges used to generate mock data for trade requests
   */
  private val MockVals = new {
    val MIN_YEAR = 2012
    val MAX_YEAR = 2015
    val MIN_SELL = 250
    val MAX_SELL = 10000
    val MIN_RATE = 250
    val MAX_RATE = 1000
  }

  /**
   * Alphabetical representation of 1. Use this to avoid Magic Number issues.
   */
  private val ONE = 1

  /**
   * Exchange rate convertor
   */
  private val rates = new OpenExchangeRates()

  /**
   * The country data containing 2 letter ISO_3166-1_alpha-2 country codes and
   * 3 letter ISO_4217 currency codes.
   *
   * @see https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
   * @see https://en.wikipedia.org/wiki/ISO_4217
   */
  private val countryData =
    """
      |<NewDataSet>
      |  <Table>
      |    <Name>Argentina</Name>
      |    <CountryCode>ar</CountryCode>
      |    <Currency>Peso </Currency>
      |    <CurrencyCode>ARS</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Australia</Name>
      |    <CountryCode>au</CountryCode>
      |    <Currency>Dollar</Currency>
      |    <CurrencyCode>AUD</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Brazil</Name>
      |    <CountryCode>br</CountryCode>
      |    <Currency>Cruzeiro</Currency>
      |    <CurrencyCode>BRC</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Canada</Name>
      |    <CountryCode>ca</CountryCode>
      |    <Currency>Dollar</Currency>
      |    <CurrencyCode>CAD</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Chile</Name>
      |    <CountryCode>cl</CountryCode>
      |    <Currency>Peso</Currency>
      |    <CurrencyCode>CLP</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>China</Name>
      |    <CountryCode>cn</CountryCode>
      |    <Currency>Yuan Renminbi</Currency>
      |    <CurrencyCode>CNY</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Denmark</Name>
      |    <CountryCode>dk</CountryCode>
      |    <Currency>Guilder</Currency>
      |    <CurrencyCode>DKK</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Germany</Name>
      |    <CountryCode>de</CountryCode>
      |    <Currency>Mark</Currency>
      |    <CurrencyCode>EUR</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Great Britain</Name>
      |    <CountryCode>gb</CountryCode>
      |    <Currency>Sterling</Currency>
      |    <CurrencyCode>GBP</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Hong Kong</Name>
      |    <CountryCode>hk</CountryCode>
      |    <Currency>Dollar</Currency>
      |    <CurrencyCode>HKD</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>India</Name>
      |    <CountryCode>in</CountryCode>
      |    <Currency>Rupee</Currency>
      |    <CurrencyCode>INR</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Ireland</Name>
      |    <CountryCode>ie</CountryCode>
      |    <Currency>Punt</Currency>
      |    <CurrencyCode>EUR</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Israel</Name>
      |    <CountryCode>il</CountryCode>
      |    <Currency>New Shekel</Currency>
      |    <CurrencyCode>ILS</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Kuwait</Name>
      |    <CountryCode>kw</CountryCode>
      |    <Currency>Dinar</Currency>
      |    <CurrencyCode>KWD</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Mexico</Name>
      |    <CountryCode>mx</CountryCode>
      |    <Currency>Peso</Currency>
      |    <CurrencyCode>MXP</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>New Zealand</Name>
      |    <CountryCode>nz</CountryCode>
      |    <Currency>Dollar</Currency>
      |    <CurrencyCode>NZD</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Norway</Name>
      |    <CountryCode>no</CountryCode>
      |    <Currency>Kroner</Currency>
      |    <CurrencyCode>NOK</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Russian Federation</Name>
      |    <CountryCode>ru</CountryCode>
      |    <Currency>Rouble</Currency>
      |    <CurrencyCode>RUB</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Singapore</Name>
      |    <CountryCode>sg</CountryCode>
      |    <Currency>Dollar</Currency>
      |    <CurrencyCode>SGD</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>South Africa</Name>
      |    <CountryCode>za</CountryCode>
      |    <Currency>Rand</Currency>
      |    <CurrencyCode>ZAR</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Spain</Name>
      |    <CountryCode>es</CountryCode>
      |    <Currency>Peseta</Currency>
      |    <CurrencyCode>EUR</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Sweden</Name>
      |    <CountryCode>se</CountryCode>
      |    <Currency>Krona</Currency>
      |    <CurrencyCode>SEK</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Switzerland</Name>
      |    <CountryCode>ch</CountryCode>
      |    <Currency>Franc</Currency>
      |    <CurrencyCode>CHF</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>Turkey</Name>
      |    <CountryCode>tr</CountryCode>
      |    <Currency>Lira</Currency>
      |    <CurrencyCode>TRL</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>United Arab Emirates</Name>
      |    <CountryCode>ae</CountryCode>
      |    <Currency>Dirham</Currency>
      |    <CurrencyCode>AED</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>United Kingdom</Name>
      |    <CountryCode>uk</CountryCode>
      |    <Currency>Pound</Currency>
      |    <CurrencyCode>GBP</CurrencyCode>
      |  </Table>
      |  <Table>
      |    <Name>United States</Name>
      |    <CountryCode>us</CountryCode>
      |    <Currency>Dollar</Currency>
      |    <CurrencyCode>USD</CurrencyCode>
      |  </Table>
      |</NewDataSet>
    """.stripMargin

  /**
   * Country data loaded from XML
   */
  private val elem = scala.xml.XML.loadString(countryData)

  /**
   * Country data processed into a [[scala.collection.SeqView]] of MockCountryData
   */
  private val countries = (elem \ "Table").view.map(e =>
    MockCountryData((e \ "CountryCode").text.toUpperCase, (e \ "CurrencyCode").text)).filter(_.currencyCode != "").filter(rates.isCurrencySupported(_))

  /**
   * Retrieve a random MockCountryData
   *
   * @param compareCountry the country code to compare. This parameter ensures that the returned
   * MockCountryData will not be the same country as the compareCountry.
   * @param compareCurrency the currency code to compare. This parameter ensures that the returned
   * MockCountryData will not have the same currency as the compareCurrency.
   * @param matchCurrency determines if countires with the same currency should be matched.
   * @return MockCountryData a randomly selected MockCountryData
   */
  private def getRandomSource(compareCountry: String, compareCurrency: String, matchCurency: Boolean): MockCountryData = {
    import scala.annotation.tailrec
    @tailrec
    def innerLoop(s1: String, s2: String, b1: Boolean): MockCountryData = {
      val rand = Seq.fill(ONE)(Random.nextInt(countries.size)).head
      countries.zipWithIndex.find(_._2 == rand) match {
        case Some(e) if !matchCurency && !e._1.countryCode.equalsIgnoreCase(s1) && !e._1.currencyCode.equalsIgnoreCase(s2) => e._1
        case Some(e) if matchCurency && e._1.currencyCode.equalsIgnoreCase(s2) => e._1
        case Some(e) => innerLoop(s1, s2, b1)
        case None => innerLoop(s1, s2, b1)
      }
    }
    innerLoop(compareCountry, compareCurrency, matchCurency)
  }

  /**
   * Retrieve a random [[scala.Double]] with a range
   *
   * @param min the start of the range
   * @param max the end of the range
   * @return Double the randomly generated number
   */
  private def getRandomNumber(min: Double, max: Double) = {
    (min + (max - min) * Random.nextDouble()).toString
  }

  /**
   * Retrieve a random String with a length of 8 chars
   *
   * @return String the randomly generated String with a length of 8 chars
   */
  private def getRandomUserId() = {
    val x = Random.alphanumeric
    x take 8 mkString ("")
  }

  /**
   * Retrieve a random [[org.joda.time.DateTime]]
   *
   * @return DateTime the randomly generated DateTime
   */
  private def getRandomDateTime() = {
    import java.util.{ Calendar, GregorianCalendar }
    val cal = new GregorianCalendar()
    cal.set(Calendar.YEAR, Random.nextInt((MockVals.MAX_YEAR - MockVals.MIN_YEAR) + ONE) + MockVals.MIN_YEAR)
    cal.set(Calendar.DAY_OF_YEAR, Random.nextInt((cal.getActualMaximum(Calendar.DAY_OF_YEAR) - ONE) + ONE) + ONE)
    new org.joda.time.DateTime(cal)
  }

  /**
   * Produces a Trade that contains randomly generated mock data
   */
  private def getMockTrade(): Trade = {
    // build the Trade object, using random data
    val currencyFrom = getRandomSource("", "", false)
    val currencyTo = getRandomSource("", currencyFrom.currencyCode, false)
    val originatingCountry = getRandomSource("", "", false)
    val amountSell = new JBigDecimal(getRandomNumber(MockVals.MIN_SELL, MockVals.MAX_SELL)).setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)
    val rate = {
      val base = new JBigDecimal(getRandomNumber(MockVals.MIN_RATE, MockVals.MAX_RATE))
      base.divide(new JBigDecimal("1000")).setScale(DECIMAL_PLACES_4, ROUNDING_STRATEGY)
    }
    val amountBuy = amountSell.multiply(rate).setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY)
    // the Trade object, created with randomly generated data
    new Trade(getRandomUserId(), currencyFrom.currencyCode, currencyTo.currencyCode, amountSell,
      amountBuy, rate, getRandomDateTime(), originatingCountry.countryCode)
  }

  /**
   * POST a randomly generated Trade
   */
  private def sendRandomPostData = {
    val logRequest: HttpRequest => HttpRequest = { r => log.debug(r.toString); r }
    val logResponse: HttpResponse => HttpResponse = { r => log.debug(r.toString); r }
    val pipeline = logRequest ~> sendReceive ~> logResponse
    val mockTrade = getMockTrade()
    val response = pipeline(Post(apiMockTrade, mockTrade))
    // handle result/failure
    response.onComplete {
      case Success(response) => log.debug("Mock Trade POST SUCCESS " + response.toString)
      case Failure(e) => log.error("Mock Trade POST FAILURE: " + e)
    }
    mockTrade
  }

  /**
   * POST a series of randomly generated Trade
   */
  private def sendBulkPostData = {
    val logRequest: HttpRequest => HttpRequest = { r => log.debug(r.toString); r }
    val logResponse: HttpResponse => HttpResponse = { r => log.debug(r.toString); r }
    val pipeline = logRequest ~> sendReceive ~> logResponse
    val mockTrades = ONE to apiMockMax map (i => getMockTrade)
    mockTrades.foreach(mockTrade => {
      val response = pipeline(Post(apiMockTrade, mockTrade))
      // handle result/failure
      response.onComplete {
        case Success(response) => log.debug("Mock Trade POST SUCCESS " + response.toString)
        case Failure(e) => log.error("Mock Trade POST FAILURE: " + e)
      }
    })
    mockTrades.size
  }

}
