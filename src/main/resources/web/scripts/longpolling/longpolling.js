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
 * Provides long polling data retrieval functionalities for browsers
 * that do not support web sockets/web workers
 *
 * @class LongPolling
 * @static
 */
var LongPolling = new function() {
	
/**
 * Registers the client for long polling updates from server side web services
 *
 * @method activateLongPolling
 * @return {Unit}
 */
 this.activateLongPolling = function() {

    // repeating, self executing function to retrieve data about latest currency trades
    (function latestTrades() {
    	setTimeout(function() {
    		$.ajax({
    			type: "GET",
    			url: Api.URL_LATEST,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateTradesLongPolling(data.data);
                }, 
                dataType: "json", 
                complete: latestTrades
            });
    	}, 2000);
    })();

    // repeating, self executing function to retrieve data about currency sales volume by country
    (function countriesVolume() {
    	setTimeout(function() {
    		$ .ajax({
    			type: "GET",
    			url: Api.URL_COUNTRIESVOLUME,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateCountriesVolume(data.data, false, false);
                }, 
                dataType: "json", 
                complete: countriesVolume
            });
    	}, 6000);
    })();

    // repeating, self executing function to retrieve data about currencies sold by volume
    (function currenciesSoldByVolume() {
    	setTimeout(function() {
    		$ .ajax({
    			type: "GET",
    			url: Api.URL_SELLVOLUME,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateTradeVolume(data.data, "sell-volume", false, false);
                }, 
                dataType: "json", 
                complete: currenciesSoldByVolume
            });
    	}, 4000);
    })();

    // repeating, self executing function to retrieve data about currencies sold by value
    (function currenciesSoldByValue() {
    	setTimeout(function() {
    		$ .ajax({
    			type: "GET",
    			url: Api.URL_SELLVALUE,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateTradeValue(data.data, "sell-value", false, false);
                }, 
                dataType: "json", 
                complete: currenciesSoldByValue
            });
    	}, 4000);
    })();

    // repeating, self executing function to retrieve data about currencies bought by volume
    (function currenciesBoughtByVolume() {
    	setTimeout(function() {
    		$ .ajax({
    			type: "GET",
    			url: Api.URL_BUYVOLUME,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateTradeVolume(data.data, "buy-volume", false, false);
                }, 
                dataType: "json", 
                complete: currenciesBoughtByVolume
            });
    	}, 4000);
    })();

    // repeating, self executing function to retrieve data about currencies bought by value
    (function currenciesBoughtByValue() {
    	setTimeout(function() {
    		$ .ajax({
    			type: "GET",
    			url: Api.URL_BUYVALUE,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateTradeValue(data.data, "buy-value", false, false);
                }, 
                dataType: "json", 
                complete: currenciesBoughtByValue
            });
    	}, 4000);
    })();

    // repeating, self executing function to retrieve data about currency pairs by sales volume
    (function currencyPairs() {
    	setTimeout(function() {
    		$.ajax({
    			type: "GET",
    			url: Api.URL_CURRENCYPAIR,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateCurrencyPairs(data.data, false, false);
                }, 
                dataType: "json", 
                complete: currencyPairs
            });
    	}, 6000);
    })();

};

/**
 * Registers the client for long polling map updates from server side web service
 *
 * @method activateMapUpdates
 * @return {Unit}
 */
 this.activateMapUpdates = function() {

    // repeating, self executing function to retrieve data about country codes from which trades have been placed
    (function mapUpdates() {
    	setTimeout(function () {
    		$.ajax({
    			type: "GET",
    			url: Api.URL_COUNTRYCODES,
                cache: false, // ensure to set this to false otherwise IE will not retrieve the latest data
                success: function (data) {
                	UiUtils.updateCountriesInfo(data.data, false, false);
                }, 
                dataType: "json", 
                complete: mapUpdates
            });
    	}, 30000);
    })();
    
};

}
