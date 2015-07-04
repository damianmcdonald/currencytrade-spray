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

package currencytrade.xrates

import akka.event.slf4j.SLF4JLogging
import currencytrade.Configuration._
import currencytrade.mock.MockCountryData
import net.liftweb.json._
import java.math.{ BigDecimal => JBigDecimal }

/**
 * Trait describing the methods to provide currency exchange rate conversions
 */
trait XRates {
  def convertToBaseCurrency(currency: String, amount: JBigDecimal): Option[JBigDecimal]
  def getRateByCurrency(currency: String): Option[Double]
  def isCurrencySupported(mock: MockCountryData): Boolean
}

/**
 * Provides currency exchange rate conversions using Open Exchange Rates data.
 *
 * @see https://openexchangerates.org/
 */
class OpenExchangeRates extends XRates with SLF4JLogging {

  /**
   * Checks if a currency is supported by the Open Exchange Rates data
   *
   * @param mock a [[currencytrade.mock.MockCountryData]] that contains the currency top check
   * @return Boolean true if the currency is supported by Open Exchange Rates data, false if not
   */
  def isCurrencySupported(mock: MockCountryData): Boolean = {
    val xs = for { JDouble(x) <- (EXCHANGE_RATES \\ mock.currencyCode) } yield x
    xs match {
      case x :: xs => true
      case Nil => false //return false if currency is not found
    }
  }

  /**
   * Gets the exchange rate for a currency.
   * Currencies are defined as a 3 letter (ISO_4217) currency code.
   *
   * @see https://en.wikipedia.org/wiki/ISO_4217
   *
   * @param currency a 3 letter (ISO_4217) currency code representing the currency for which to get the exchange rate
   * @return Option[Double] Some(Double) if rate is found, None if not
   */
  def getRateByCurrency(currency: String): Option[Double] = {
    val xs = for { JDouble(x) <- (EXCHANGE_RATES \\ currency) } yield x
    xs match {
      case x :: xs => Some(x.toString.toDouble)
      case Nil => None //return None if currency is not found
    }
  }

  /**
   * Converts a currency to a base currency. Base currency is USD by default.
   * Currencies are defined as a 3 letter (ISO_4217) currency code.
   *
   * @see https://en.wikipedia.org/wiki/ISO_4217
   *
   * @param currency a 3 letter (ISO_4217) currency code representing the currency
   * @param value the currency value to convert to the base currency
   * @return Option[java.math.BigDecimal] Some(java.math.BigDecimal) if rate is found, None if not
   */
  def convertToBaseCurrency(currency: String, value: JBigDecimal): Option[JBigDecimal] = {
    getRateByCurrency(currency) match {
      case Some(rate: Double) => Some(value.multiply(new JBigDecimal(rate)).setScale(DECIMAL_PLACES_2, ROUNDING_STRATEGY))
      case _ => None
    }
  }

  /**
   * Static list of relative exchange rate data from Open Exchange Rates, using USD as the base currency.
   *
   * @see https://openexchangerates.org/
   */
  private val EXCHANGE_RATES = {
    parse(
      """
        |{
        |"rates": {
        |"AED": 3.673004,
        |"AFN": 58.935001,
        |"ALL": 125.416299,
        |"AMD": 477.779997,
        |"ANG": 1.788775,
        |"AOA": 110.399166,
        |"ARS": 9.01533,
        |"AUD": 1.298912,
        |"AWG": 1.793333,
        |"AZN": 1.04965,
        |"BAM": 1.735746,
        |"BBD": 2.0000,
        |"BDT": 78.32549,
        |"BGN": 1.739761,
        |"BHD": 0.377055,
        |"BIF": 1561.815,
        |"BMD": 1.0000,
        |"BND": 1.348596,
        |"BOB": 6.925693,
        |"BRL": 3.140786,
        |"BSD": 1.0000,
        |"BTC": 0.0044561711,
        |"BTN": 63.9226,
        |"BWP": 10.049438,
        |"BYR": 15123,
        |"BZD": 1.99788,
        |"CAD": 1.251151,
        |"CDF": 925.75075,
        |"CHF": 0.933635,
        |"CLF": 0.024602,
        |"CLP": 628.504099,
        |"CNY": 6.185454,
        |"COP": 2582.38334,
        |"CRC": 535.955795,
        |"CUC": 1.0000,
        |"CUP": 1.003088,
        |"CVE": 98.1556977,
        |"CZK": 24.38066,
        |"DJF": 177.278752,
        |"DKK": 6.63542,
        |"DOP": 45.05964,
        |"DZD": 98.314889,
        |"EEK": 13.8963,
        |"EGP": 7.630897,
        |"ERN": 15.14,
        |"ETB": 20.62243,
        |"EUR": 0.89018,
        |"FJD": 2.059867,
        |"FKP": 0.653104,
        |"GBP": 0.653104,
        |"GEL": 2.2901,
        |"GGP": 0.653104,
        |"GHS": 4.165441,
        |"GIP": 0.653104,
        |"GMD": 43.07369,
        |"GNF": 7461.155098,
        |"GTQ": 7.685832,
        |"GYD": 205.507169,
        |"HKD": 7.753339,
        |"HNL": 21.90723,
        |"HRK": 6.723776,
        |"HTG": 48.0983,
        |"HUF": 277.202897,
        |"IDR": 13283.416667,
        |"ILS": 3.84129,
        |"IMP": 0.653104,
        |"INR": 63.89612,
        |"IQD": 1183.197464,
        |"IRR": 28902.0000,
        |"ISK": 131.512999,
        |"JEP": 0.653104,
        |"JMD": 115.9864,
        |"JOD": 0.708544,
        |"JPY": 124.707799,
        |"KES": 96.337641,
        |"KGS": 58.5285,
        |"KHR": 4097.287549,
        |"KMF": 437.07313,
        |"KPW": 899.91,
        |"KRW": 1111.919982,
        |"KWD": 0.302064,
        |"KYD": 0.818568,
        |"KZT": 186.1646,
        |"LAK": 8151.827305,
        |"LBP": 1508.295,
        |"LKR": 134.9072,
        |"LRD": 84.580002,
        |"LSL": 12.43789,
        |"LTL": 3.039343,
        |"LVL": 0.624093,
        |"LYD": 1.37937,
        |"MAD": 9.671704,
        |"MDL": 18.38571,
        |"MGA": 3173.4225,
        |"MKD": 54.77357,
        |"MMK": 1093.723325,
        |"MNT": 1887.166667,
        |"MOP": 7.995069,
        |"MRO": 310.33175,
        |"MTL": 0.683738,
        |"MUR": 34.968,
        |"MVR": 15.287533,
        |"MWK": 440.258539,
        |"MXN": 15.56962,
        |"MYR": 3.720425,
        |"MZN": 37.30,
        |"NAD": 12.43789,
        |"NGN": 199.450299,
        |"NIO": 26.976,
        |"NOK": 7.847785,
        |"NPR": 101.72105,
        |"NZD": 1.404236,
        |"OMR": 0.385054,
        |"PAB": 1.0000,
        |"PEN": 3.150462,
        |"PGK": 2.738525,
        |"PHP": 44.95081,
        |"PKR": 101.9929,
        |"PLN": 3.695097,
        |"PYG": 5119.81,
        |"QAR": 3.640741,
        |"RON": 3.958076,
        |"RSD": 107.07498,
        |"RUB": 55.91877,
        |"RWF": 693.227875,
        |"SAR": 3.750296,
        |"SBD": 7.832117,
        |"SCR": 13.35285,
        |"SDG": 5.983081,
        |"SEK": 8.321565,
        |"SGD": 1.347641,
        |"SHP": 0.653104,
        |"SLL": 4371.50,
        |"SOS": 705.440997,
        |"SRD": 3.2825,
        |"STD": 21800.475,
        |"SVC": 8.683151,
        |"SYP": 188.826003,
        |"SZL": 12.43839,
        |"THB": 33.73653,
        |"TJS": 6.26435,
        |"TMT": 3.5001,
        |"TND": 1.932981,
        |"TOP": 2.05854,
        |"TRY": 2.668605,
        |"TTD": 6.355445,
        |"TWD": 30.89099,
        |"TZS": 2163.251634,
        |"UAH": 21.11818,
        |"UGX": 3078.268333,
        |"USD": 1.0000,
        |"UYU": 26.88057,
        |"UZS": 2535.27002,
        |"VEF": 6.318668,
        |"VND": 21814.166667,
        |"VUV": 105.23,
        |"WST": 2.470836,
        |"XAF": 583.070965,
        |"XAG": 0.061977,
        |"XAU": 0.000851,
        |"XCD": 2.70102,
        |"XDR": 0.711007,
        |"XOF": 583.075065,
        |"XPF": 106.059899,
        |"YER": 214.9524,
        |"ZAR": 12.41711,
        |"ZMK": 5252.024745,
        |"ZMW": 7.21697,
        |"ZWL": 322.322775
        |}
        |}
        """.stripMargin
    )
  }

}
