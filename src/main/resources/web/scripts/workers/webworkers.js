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
 * Creates and manages HTML5 web workers for supported web browsers
 *
 * @class WebWorkers
 * @static
 */
var WebWorkers = new function() {

    // reference to web worker that will handle processing of latest trade data
    var LATEST_TRADES_WORKER = new Worker('scripts/workers/tradesworker.js');
    // reference to web worker that will handle processing of currencies sold by volume data
    var SOLD_VOLUME_WORKER = new Worker('scripts/workers/dataworker.js');
    // reference to web worker that will handle processing of currencies sold by value data
    var SOLD_VALUE_WORKER = new Worker('scripts/workers/dataworker.js');
    // reference to web worker that will handle processing of currencies bought by volume data
    var BOUGHT_VOLUME_WORKER = new Worker('scripts/workers/dataworker.js');
    // reference to web worker that will handle processing of currencies bought by value data
    var BOUGHT_VALUE_WORKER = new Worker('scripts/workers/dataworker.js');
    // reference to web worker that will handle processing of currency pairs data
    var CURRENCY_PAIRS_WORKER = new Worker('scripts/workers/dataworker.js');
    // reference to web worker that will handle processing of sales volume by country data
    var COUNTRY_VOLUME_WORKER = new Worker('scripts/workers/dataworker.js');

    /**
     * Returns the reference to the web worker that handles the processing of latest trade data
     *
     * @method getLatestTradesWorker
     * @return {Worker} Web worker that handles the processing of latest trade data
     */
    this.getLatestTradesWorker = function() {
        return LATEST_TRADES_WORKER;
    };

    /**
     * Returns the reference to the web worker that handles the processing of currencies sold by volume data
     *
     * @method getSoldVolumeWorker
     * @return {Worker} Web worker that handles the processing of currencies sold by volume data
     */
    this.getSoldVolumeWorker = function() {
        return SOLD_VOLUME_WORKER;
    };

    /**
     * Returns the reference to the web worker that handles the processing of currencies sold by value data
     *
     * @method getSoldValueWorker
     * @return {Worker} Web worker that handles the processing of currencies sold by value data
     */
    this.getSoldValueWorker = function() {
        return SOLD_VALUE_WORKER;
    };

    /**
     * Returns the reference to the web worker that handles the processing of currencies bought by volume data
     *
     * @method getBoughtVolumeWorker
     * @return {Worker} Web worker that handles the processing of currencies bought by volume data
     */
    this.getBoughtVolumeWorker = function() {
        return BOUGHT_VOLUME_WORKER;
    };

    /**
     * Returns the reference to the web worker that handles the processing of currencies bought by value data
     *
     * @method getBoughtValueWorker
     * @return {Worker} Web worker that handles the processing of currencies bought by value data
     */
    this.getBoughtValueWorker = function() {
        return BOUGHT_VALUE_WORKER;
    };

    /**
     * Returns the reference to the web worker that handles the processing of currency pairs data
     *
     * @method getCurrencyPairsWorker
     * @return {Worker} Web worker that handles the processing of currency pairs data
     */
    this.getCurrencyPairsWorker = function() {
        return CURRENCY_PAIRS_WORKER;
    };

    /**
     * Returns the reference to the web worker that handles the processing of sales volume by country data
     *
     * @method getCountryVolumeWorker
     * @return {Worker} Web worker that handles the processing of sales volume by country data
     */
    this.getCountryVolumeWorker = function() {
        return COUNTRY_VOLUME_WORKER;
    };

    /**
     * Registers message event listeners for web worker events
     *
     * @method registerWebWorkers
     * @return {Unit}
     */
    this.registerWebWorkers = function() {

        LATEST_TRADES_WORKER.addEventListener('message', function(e) {
            UiUtils.updateTrades(e.data);
        }, false);

        SOLD_VOLUME_WORKER.addEventListener('message', function(e) {
            UiUtils.updateTradeVolume(e.data.data, e.data.elementId, false, true);
        }, false);

        SOLD_VALUE_WORKER.addEventListener('message', function(e) {
            UiUtils.updateTradeValue(e.data.data, e.data.elementId, false, true);
        }, false);

        BOUGHT_VOLUME_WORKER.addEventListener('message', function(e) {
            UiUtils.updateTradeVolume(e.data.data, e.data.elementId, false, true);
        }, false);

        BOUGHT_VALUE_WORKER.addEventListener('message', function(e) {
            UiUtils.updateTradeValue(e.data.data, e.data.elementId, false, true);
        }, false);

        CURRENCY_PAIRS_WORKER.addEventListener('message', function(e) {
            UiUtils.updateCurrencyPairs(e.data.data, false, true);
        }, false);

        COUNTRY_VOLUME_WORKER.addEventListener('message', function(e) {
            UiUtils.updateCountriesVolume(e.data.data, false, true);
        }, false);

    };

}
