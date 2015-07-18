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
 * A utiltity that contains functions to update the UI and handle UI events
 *
 * @class UiUtils
 * @static
 */
var UiUtils = new function() {

    /**
    * The pie chart displaying currency sales volume by country
    *
    * @property PIE_CHART
    */
    var PIE_CHART;

    /**
     * The maximum time to wait {Long} for the map data to be retrieved
     *
     * @property MAP_LOAD_WAIT_TIME
     */
    var MAP_LOAD_WAIT_TIME = 15000;

    /**
     * Parses a {Date} into a formatted string
     *
     * @method parseDate
     * @param d {Date}
     * @return {String} representation of the {Date} as dd-MMM-yyyy HH:mm:ss
     */
    function parseDate(date) {
        // inner helper function to return a 2 digit number
        // padding numbers less that 10 with a 0
        function formatDateNumber(n) {
            if(n>9) return n;
            return "0"+n;
        }
        // array of abbreviations for the months of the year
        var monthNames = [ "Jan", "Feb", "Mar", "April", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec" ];
        // the formatted date string represented by the pattern; dd-MMM-yyyy HH:mm:ss
        return formatDateNumber(date.getDate()) +'-'+monthNames[date.getMonth()]+'-'+date.getFullYear() +' '+
            formatDateNumber(date.getHours()) +':'+formatDateNumber(date.getMinutes())+':'+ formatDateNumber(date.getSeconds());
    }

    /**
     * Handles the click event for the Random Trade UI button.
     * Calls the mock trade web service to place a random currency trade.
     *
	 * API json response
	 *
	 * {
	 * 	"event":"TRADE_PERSISTED",
	 *  "data":
	 *		{
	 *	     "userId":"ap8X8PES",
	 *		 "currencyFrom":"CNY",
	 *		 "currencyTo":"KWD",
	 *		 "amountSell":4469.70,
	 *		 "amountBuy":1904.54,
	 *		 "rate":0.4261,
	 *		 "timePlaced":"16-Jun-12 13:40:18",
	 *		 "originatingCountry":"UK"
	 *		}
	 * }
	 *
     * @method postRandomTradeData
     * @return {Unit}
     */
    this.postRandomTradeData = function() {
        // disable the button to limit users to one request/response cycle per click
        // i.e. the user has to wait for the web service call to finish before
        // making another request
        $('#random-post-btn').prop('disabled', true);

        // ajax post request to mock trade web service
        $ .ajax({
            type: "POST",
            url: Api.URL_MOCKTRADE,
            cache: false,
            dataType: "json"
        }).done(function(data) {
			var trade = data.data;
            // create dynamic html for insertion into HTML DOM
            var html = "";
            // p tag containg formatted date
            html += "<p>Trade placed at: "+parseDate(new Date())+"</p>";
            // br tag to create a new line
            html += "<br/>";
            // open table
            html += "<table id=\"latest-trades-table\" class=\"table table-striped table-condensed\">";

            // open table rows
            html += "<tr><td>User:</td><td>"+trade.userId+"</td></tr>";
            html += "<tr><td>Currency From:</td><td>"+trade.currencyFrom+"</td></tr>";
            html += "<tr><td>Currency To:</td><td>"+trade.currencyTo+"</td></tr>";
            html += "<tr><td>Amount Sold:</td><td>"+trade.amountSell+"</td></tr>";
            html += "<tr><td>Amount Bought:</td><td>"+trade.amountBuy+"</td></tr>";
            html += "<tr><td>Rate:</td><td>"+trade.rate+"</td></tr>";
            html += "<tr><td>Time Placed:</td><td>"+trade.timePlaced+"</td></tr>";
            html += "<tr><td>Originating Country:</td><td>"+trade.originatingCountry+"</td></tr>";
            // close table rows

            // close table
            html += "</table>";

            // write the html to the UI
            $("#post-data").html(html);
        }).always(function() {
            // regardless of the response time from the ajax request
            // always re-enable the button
            setTimeout(function(){ $('#random-post-btn').prop('disabled', false);  }, 250);
        });
    };

    /**
     * Handles the click event for the Bulk Trades UI button.
     * Calls the bulk trades web service to place random currency trades.
     *
	 * API json response
	 *
	 * {
	 *	"event":"LATEST_TRADES", 
	 *	"data":[
	 *		{ 
	 * 		"currencyFrom" : "CNY" , 
	 *		"currencyTo" : "KWD" , 
	 *		"amountSell" : 4469.7 , 
	 *		"amountBuy" : 1904.54 , 
	 *		"rate" : 0.4261 , 
	 *		"timePlaced" : 
	 *			{ 
	 *			"$date" : "2012-06-16T11:40:18.000Z"
	 *			}, 
	 *		"originatingCountry" : "UK" , 
	 *		"receptionDate" : 
	 *			{	 
	 *			"$date" : "2015-07-13T11:40:18.149Z"
	 *			}
	 *		}
	 *	]
	 * }
	 *
     * @method postBulkTradesData
     * @return {Unit}
     */
    this.postBulkTradesData = function() {
        // disable the button to limit users to one request/response cycle per click
        // i.e. the user has to wait for the web service call to finish before
        // making another request
        $('#bulk-post-btn').prop('disabled', true);

        $ .ajax({
            type: "POST",
            url: Api.URL_BULKTRADES,
            cache: false,
            dataType: "json"
        }).done(function(data) {
            // create dynamic html for insertion into HTML DOM
            var html = "";
            // p tag containg formatted date
            html += "<p>Bulk trades placed at: "+parseDate(new Date())+"</p>";
            // br tag to create a new line
            html += "<br/>";
            // p tag containg the number of bulke trades that have been placed
            html += "<p>"+data+" new trades have been placed.</p>";

            // write the html to the UI
            $("#post-data").html(html);
        }).always(function() {
            // regardless of the response time from the ajax request
            // always re-enable the button
            setTimeout(function(){ $('#bulk-post-btn').prop('disabled', false);  }, 1000);
        });
    };

    /**
     * Recalculates the offset values for intra page navigation of 'a[href*=#] tags.
     * This method is required because data is dynamically loaded into the page and
     * changes the offset values that are calculated on page load. This has a specific
     * impact on the #map-header div.
     *
     * @method recaclculateOffsets
     * @return {Unit}
     */
    this.recaclculateOffsets = function() {
        // establish handler for intra-page navigation and scroll animations
        $('a[href*=#]').each(function () {
            if (location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '')
                && location.hostname == this.hostname && this.hash.replace(/#/, '')) {
                var targetId = $(this.hash), targetAnchor = $('[name=' + this.hash.slice(1) + ']');
                var target = targetId.length ? targetId : targetAnchor.length ? targetAnchor : false;
                if (target) {
                    if (targetId.length > 0) {
                        // when we encounter the map-header link, reset the offset based on the latest values
                        if (targetId[0].id === "map-header") {
                            var targetOffset = target.offset().top;
                            // unbind the click event from the map-header div as it is not required
                            $(this).unbind("click");
                            // animate and move to the map-header div
                            $('html, body').animate({scrollTop: targetOffset}, 1000);
                        }
                    }
                }
            }
        });
    };

    /**
     * Updates the UI with the latest trade data relating to currency sales/purchase volumes
     *
     * The method accepts a TradeVolumeEvent represented in json as shown below:
     *
     * var tradeVolumeEvent = {
     *   "event":"CURRENCIES_SOLD_VOLUME" / , "CURRENCIES_BOUGHT_VOLUME"
     *   "data":[
     *       {   "currency":"AUD",
     *           "volume":10
     *       },{ "currency":"TRY",
     *           "volume":15
     *       },{ "currency":"EUR",
     *           "volume":20
     *       }
     *   ]
     * }
     *
     * updateTradeVolume(tradeVolumeEvent, "someElementId", false);
     *
     * @method updateTradeVolume
     * @param data {Object}
     * @param id {String} html id of the element to update with the data
     * @param updateProgress {Boolean} indicates if the progress bar should be updated
     * @return {Unit}
     */
    this.updateTradeVolume = function(data, id, updateProgress, partial) {
        if(partial) {
            $("#"+id).html(data.data);
            return;
        }
        // create dynamic html for insertion into HTML DOM
        var html = "";
        // open table
        html += "<table class=\"table table-striped table-condensed\">";
        // create table headers
        html += "<tr><th>Currency</th><th>Total trades</th></tr>";
        // iterate each data element and create a table row
        $.each(data, function (i, v) {
            html += "<tr><td>" + v.currency + "</td><td>" + v.volume + "</td></tr>";
        });
        // close table
        html += "</table>";
        // update the element with dynamically generated html
        $("#"+id).html(html);
        // update the progress bar if required
        if(updateProgress) ProgressBar.update();
    };

    /**
     * Updates the UI with the latest trade data received via long polling
     *
     * The method accepts a LatestTradesEvent represented in json as shown below:
     *
     * var latestTradesEvent = {
     *  "event":"LATEST_TRADES",
     *  "data":[
     *     {   "rate":0.7471,
     *         "currencyTo":"USD",
     *          "amountSell":4325.89,
     *          "timePlaced":"18-Jun-15 16:19:29",
     *          "amountBuy":2343.16,
     *          "originatingCountry":"TR",
     *          "receptionDate":1434636129000,
     *          "currencyFrom":"AUD"
     *      },
     *      {   "rate":0.2580,
     *          "currencyTo":"NZD",
     *          "amountSell":3214.68,
     *          "timePlaced":"18-Jun-15 16:19:29",
     *          "amountBuy":7533.87,
     *          "originatingCountry":"AU",
     *          "receptionDate":1434636129000,
     *          "currencyFrom":"TRY"
     *      }
     *  ]
     * }
     *
     * updateTradesLongPolling(latestTradesEvent);
     *
     * @method updateTradesLongPolling
     * @param data {Object}
     * @return {Unit}
     */
    this.updateTradesLongPolling = function(data) {
        // sort the data array, by reception date, in descending order
        var sortedArray = data.sort(function(x, y){
            return y.receptionDate - x.receptionDate;
        });

        // create dynamic html for insertion into HTML DOM
        var html = "";
        // p tag containg formatted date
        html += "<p>Latest Trades: "+parseDate(new Date())+"</p>";
        // open table
        html += "<table id=\"latest-trades-table\" class=\"table table-striped table-condensed\">";
        // create table headers row
        html += "<tr><th>From</th><th>Sold</th><th>To</th><th>Bought</th><th>Rate</th><th>Time</th><th>Country</th></tr>";
        // iterate latest trades
        $.each(sortedArray, function (i, v) {
			// format the date
			var jdate =  (v.timePlaced.$date).replace("T"," ").substring(0, (v.timePlaced.$date).length - 5); 
            // create trade table row
            html += "<tr><td>" + v.currencyFrom + "</td><td>" + v.amountSell + "</td><td>" + v.currencyTo + "</td><td>" + v.amountBuy + "</td><td>" + v.rate + "</td><td>" + jdate + "</td><td>" + v.originatingCountry + "</td></tr>";
        });
        // close table
        html += "</table>";
        // insert the table into the #latest-trades elelemt
        $('#latest-trades').html(html);
    };

    /**
     * Updates the UI with the most recent trade data
     *
     * updateCurrencyPairs(currencyPairsEvent, false);
     *
     * @method updateTrades
     * @param data {String} html representation of the most recent trade, produced via tradeworker.js
     * @return {Unit}
     */
    this.updateTrades = function(data) {
        if($('#latest-trades-table tr').length > 50) {
            $("#latest-trades-table").find("tr:gt(12)").remove();
        }
        // write the html to the UI
        $("#latest-trade-data-row").after(data);
    }

    /**
     * Updates the UI bar graph with the latest currency pairs by sales volume data received
     *
     * The method accepts a CurrencyPairsEvent represented in json as shown below:
     *
     * var currencyPairsEvent = {
     *  "event":"CURRENCY_PAIRS",
     *  "data":[
     *      {   "volume" : 22 , 
	 *			"currencyFrom" : "EUR" , 
	 *			"currencyTo" : "CAD"
	 *		},{ "volume" : 22 , 
	 *			"currencyFrom" : "GBP" , 
	 *			"currencyTo" : "ZAR"
	 *		},{ "volume" : 17 , 
	 *			"currencyFrom" : "BRC" , 
	 *			"currencyTo" : "NZD"
	 *		}
     *  ]
     * }
     *
     * updateCurrencyPairs(currencyPairsEvent, false);
     *
     * @method updateCurrencyPairs
     * @param data {Object}
     * @param updateProgress {Boolean} indicates if the progress bar should be updated
     * @return {Unit}
     */
    this.updateCurrencyPairs = function(data, updateProgress, partial) {
        // unbind any previously-attached event handlers from the chart element
        $("#currency-pair-chart").unbind();

        // array to hold the graph data
        var graphData;

        if(!partial) {
            graphData = new Array();
            // put the currency pair data into a format useable by the flot bar graph
            $.each(data, function (i, v) {
                graphData.push(new Array(v.currencyFrom+"-"+v.currencyTo, v.volume));
            });
        } else {
            graphData = data;
        }

        // options for the flot bar graph
        var options = {
            series: {
                bars: {
                    show: true,
                    barWidth: 0.6,
                    fill:1,
                    align: "center"
                }
            },
            axisLabels: {
                show: true
            },
            xaxes: [{
                axisLabel: 'Currency Buy-Sell pairs'
            }],
            yaxes: [{
                position: 'left',
                axisLabel: 'Total trades'
            }],
            xaxis: {
                mode: "categories",
                tickLength: 0
            },
            yaxis: {
                minTickSize: 1
            },
            colors: ["#306d9f"]
        };

        // create the flot bar graph with the latest graph data and graph options
        $.plot($("#currency-pair-chart"), [ graphData ], options);

        // update the progress bar if required
        if(updateProgress) ProgressBar.update();
    };


    /**
     * Updates the UI pie chart with the latest currency sales volume by country data received
     *
     * The method accepts a CountryVolumeEvent represented in json as shown below:
     *
     * var countryVolumeEvent = {
     *   "event":"COUNTRIES_VOLUME",
     *   "data":[
     *       {   "country":"UK",
     *           "volume":10
     *       },{ "country":"UK",
     *           "volume":15
     *       },{ "country":"UK",
     *       "volume":20
     *       }
     *   ]
     * }
     *
     * updateCountriesVolume(countryVolumeEvent, false);
     *
     * @method updateCountriesVolume
     * @param data {Object}
     * @param updateProgress {Boolean} indicates if the progress bar should be updated
     * @return {Unit}
     */
    this.updateCountriesVolume = function(data, updateProgress, partial) {

        // custom formatter for the pie chart labels
        function labelFormatter(label, series) {
            return "<div style='font-size:8pt; text-align:center; padding:2px; color:white;'>" + label + "<br/>" + Math.round(series.percent) + "%</div>";
        }

        // array to hold the graph data
        var graphData;

        if(!partial) {
            graphData = new Array();
            // put the country volume data into a format useable by the flot pie chart
            $.each(data, function (i, v) {
                graphData.push({label: v.country, data: v.volume});
            });
        } else {
            graphData = data;
        }

        // if the pie chart has already been created, update the data set and redraw the chart
        if('undefined' !== typeof PIE_CHART) {
            PIE_CHART.setData(graphData);
            PIE_CHART.setupGrid();
            PIE_CHART.draw();
            return;
        }

        // create the flot bar graph with initial graph data and graph options
        PIE_CHART = $.plot('#country-volume-chart', graphData, {
            series: {
                pie: {
                    show: true,
                    radius: 1,
                    label: {
                        show: true,
                        radius: 1,
                        formatter: labelFormatter,
                        background: {
                            opacity: 0.8
                        }
                    }
                }
            }
        });

        // update the progress bar if required
        if(updateProgress) ProgressBar.update();

    };

    /**
     * Updates the UI with the latest trade data relating to currency sales/purchase volumes
     *
     * The method accepts a TradeVolumeEvent represented in json as shown below:
     *
     * var tradeVolumeEvent = {
     *   "event":"CURRENCIES_SOLD_VOLUME" / , "CURRENCIES_BOUGHT_VOLUME"
     *   "data":[
     *       {   "currency":"AUD",
     *           "volume":10
     *       },{ "currency":"TRY",
     *           "volume":15
     *       },{ "currency":"EUR",
     *           "volume":20
     *       }
     *   ]
     * }
     *
     * updateTradeVolume(tradeVolumeEvent, "someElementId", false);
     *
     * @method updateTradeVolume
     * @param data {Object}
     * @param id {String} html id of the element to update with the data
     * @param updateProgress {Boolean} indicates if the progress bar should be updated
     * @return {Unit}
     */
    this.updateTradeVolume = function(data, id, updateProgress, partial) {
        if(partial) {
            $("#"+id).html(data);
            return;
        }
        // create dynamic html for insertion into HTML DOM
        var html = "";
        // open table
        html += "<table class=\"table table-striped table-condensed\">";
        // create table headers
        html += "<tr><th>Currency</th><th>Total trades</th></tr>";
        // iterate each data element and create a table row
        $.each(data, function (i, v) {
            html += "<tr><td>" + v.currency + "</td><td>" + v.volume + "</td></tr>";
        });
        // close table
        html += "</table>";
        // update the element with dynamically generated html
        $("#"+id).html(html);
        // update the progress bar if required
        if(updateProgress) ProgressBar.update();
    };

    /**
     * Updates the UI with the latest trade data relating to currency sales/purchase values
     *
     * The method accepts a TradeValueEvent represented in json as shown below:
     *
     * var tradeValueEvent = {
     *   "event":"CURRENCIES_SOLD_VALUE" / , "CURRENCIES_BOUGHT_VALUE"
     *   "data":[
     *       {   "currency":"AUD",
     *           "value":5423.76
     *       },{ "currency":"TRY",
     *           "value":1285.08
     *       },{ "currency":"EUR",
     *           "value":958.42
     *       }
     *   ]
     * }
     *
     * updateTradeValue(tradeValueEvent, "someElementId", false);
     *
     * @method updateTradeValue
     * @param data {Object}
     * @param id {String} html id of the element to update with the data
     * @param updateProgress {Boolean} indicates if the progress bar should be updated
     * @return {Unit}
     */
    this.updateTradeValue = function(data, id, updateProgress, partial) {
        if(partial) {
            // update the element with dynamically generated html
            $("#"+id).html(data);
            return;
        }
        // sort the data array in descending order, based on value
        var sortedArray = data.sort(function(x, y){
            return y.value - x.value;
        });
        // create dynamic html for insertion into HTML DOM
        var html = "";
        // open table
        html += "<table class=\"table table-striped table-condensed\">";
        // create table headers
        html += "<tr><th>Currency</th><th>Total value</th></tr>";
        // iterate each data element and create a table row
        $.each(sortedArray, function (i, v) {
            html += "<tr><td>" + v.currency + "</td><td>$" + $.number( v.value, 2, '.', ','); + "</td></tr>";
        });
        // close table
        html += "</table>";
        // update the element with dynamically generated html
        $("#"+id).html(html);
        // update the progress bar if required
        if(updateProgress) ProgressBar.update();
    };

    /**
     * Updates the UI with the latest country codes data
     *
     * The method accepts a CountriesCodeEvent represented in json as shown below:
     *
     * var countriesCodeEvent = {
     *   "event":"ORIGINATING_COUNTRIES",
     *   "data":[
	 *           "AU",
     *           "TR"
     *           "ES"
     *   ]
     * }
     *
     * updateCountriesInfo(countriesCodeEvent, false, true);
     *
     * @method updateCountriesInfo
     * @param data {Object}
     * @param updateProgress {Boolean} indicates if the progress bar should be updated
     * @param repositionMap {Boolean} indicates if the map should be repositioned to fit the marker bounds
     * @return {Unit}
     */
    this.updateCountriesInfo = function(data, updateProgress, repositionMap) {
        // determine the number of countries form which a trade has been placed
        var countires_size = data.length;

        // iterate the country codes to retrieve information for each country
        $.each(data, function (i, v) {
            CountriesMap.getCountryInfoByCode(v);
        });

        // establish a start time for when map data retrieval begins
        var start_time = new Date().getTime()

        // set up an interval function, that will be fired every second until a condition is met
        // and the interval function is cleared/stopped
        var intervalId = setInterval(function() {
            // check if the number of countries corresponds to the number of coords
            // the list of coords is not exhaustive and some countries may be missing
            // if the number of countries corresponds to the number of coords then we can
            // make the call to fitMapToBounds() (if repositionMap == true) and clear the interval function
            if ( countires_size === CountriesMap.getLatLngCoords.length ) {
                if (repositionMap) { CountriesMap.fitMapToBounds(); }
                clearInterval(intervalId);
            }
            // in the event that the number of countries does not corresponds to the number of coords,
            // probably because the specific country does not exist in the coords list, it is not possible
            // to accurately determine when the map data is "fully loaded". Therefore, we take an
            // optimistic approach and "assume" that by waiting the MAP_LOAD_WAIT_TIME all the data will
            // be ready and we can make the call to fitMapToBounds() (if repositionMap == true) and clear
            // the interval function
            else if (new Date().getTime() - start_time > MAP_LOAD_WAIT_TIME) {
                if (repositionMap) { CountriesMap.fitMapToBounds(); }
                clearInterval(intervalId);
            }
        }, 1000);
        // update the progress bar if required
        if(updateProgress) ProgressBar.update();
    };

}
