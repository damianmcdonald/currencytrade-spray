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

/**
 * Describes the routes and paths of the server side web service api
 *
 * @class Api
 * @static
 */
var Api = new function() {

    /**
     * The base url path of the web service api
     *
     * @property URL_BASE
     * @final
     */
   this.URL_BASE = "/v1";

    /**
     * The url path of the mock trade web service
     *
     * @property URL_MOCKTRADE
     * @final
     */
    this.URL_MOCKTRADE = this.URL_BASE + "/mocktrade";

    /**
     * The url path of the bulk trades web service
     *
     * @property URL_BULKTRADES
     * @final
     */
    this.URL_BULKTRADES = this.URL_BASE + "/bulktrades";

    /**
     * The url path of the currency sale volume by country web service
     *
     * @property URL_COUNTRIESVOLUME
     * @final
     */
    this.URL_COUNTRIESVOLUME = this.URL_BASE + "/countriesvolume";

    /**
     * The url path of the currencies sold by volume web service
     *
     * @property URL_SELLVOLUME
     * @final
     */
    this.URL_SELLVOLUME = this.URL_BASE + "/sellvolume";

    /**
     * The url path of the currencies sold by value web service
     *
     * @property URL_SELLVALUE
     * @final
     */
    this.URL_SELLVALUE = this.URL_BASE + "/sellvalue";

    /**
     * The url path of the currencies bought by volume web service
     *
     * @property URL_BUYVOLUME
     * @final
     */
    this.URL_BUYVOLUME = this.URL_BASE + "/buyvolume";

    /**
     * The url path of the currencies bought by value web service
     *
     * @property URL_BUYVALUE
     * @final
     */
    this.URL_BUYVALUE = this.URL_BASE + "/buyvalue";

    /**
     * The url path of the currency pairs by sales volume web service
     *
     * @property URL_CURRENCYPAIR
     * @final
     */
    this.URL_CURRENCYPAIR = this.URL_BASE + "/currencypair";

    /**
     * The url path of the country codes web service
     *
     * @property URL_COUNTRYCODES
     * @final
     */
    this.URL_COUNTRYCODES = this.URL_BASE + "/countrycodes";

    /**
     * The url path of the 10 latest trades web service
     *
     * @property URL_LATEST
     * @final
     */
    this.URL_LATEST = this.URL_BASE + "/latest";

}
