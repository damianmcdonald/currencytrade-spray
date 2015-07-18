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
 * Defines the maximum number of messages to buffer before sending a 
 * message back to the main thread
 *
 * @property MAX_SIZE
 * @final
 */
var MAX_SIZE = 10;

/**
 * Defines the maximum time {Long} to wait before sending a 
 * message back to the main thread
 *
 * @property MAX_TIME
 * @final
 */
var MAX_TIME = 2000;

/**
 * Used to store the latest message updates
 *
 * @property MESSAGE_BUFFER
 */
var MESSAGE_BUFFER = "";

/**
 * Used to store the current count of updates
 *
 * @property COUNT
 */
var COUNT = 0;

/**
 * Used to store the time at which the first message was received
 *
 * @property START_TIME
 */
var START_TIME;

/**
 * Holds a reference following a call to {setInterval} in order to track
 * the interval function
 *
 * @property INTERVAL_ID
 */
var INTERVAL_ID;

/**
 * Provides functions that process worker data into the responses
 * expected by the main thread
 *
 * @class RespondWith
 * @static
 */
var RespondWith = new function() {
	// internal function to format numbers using , as a separator
	function numberWithCommas(x) {
		var parts = x.toString().split(".");
		parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
		return parts.join(".");
	}

	/**
	 * Produces an array of currency pairs by sales volume data that can 
	 * be used with a Flot.js bar graph
	 *
	 * @method currencyPairsData
	 * @return {Object} Array of data to be used in a bar graph
	 */
	this.currencyPairsData = function(data) {
		// array to hold the graph data
		var graphData = new Array();
		// put the currency pair data into a format useable by the flot bar graph
		for(var i=0; i<data.length; i++) {
			graphData.push(new Array(data[i].currencyFrom+"-"+data[i].currencyTo, data[i].volume));
		}
		return graphData;
	};

	/**
	 * Produces an array of currency sales volume by country data that can be 
	 * used with a Flot.js bar graph
	 *
	 * @method countryVolumeData
	 * @return {Object} Array of data to be used in a bar graph
	 */
	this.countryVolumeData = function(data) {
		// array to hold the graph data
		var graphData = new Array();
		// put the country volume data into a format useable by the flot pie chart
		for(var i=0; i<data.length; i++) {
			graphData.push({ label: data[i].country, data: data[i].volume });
		}
		return graphData;
	};

    /**
	 * Produces a html table that contains the latest trade volume data
	 *
	 * @method tradeVolumeData
	 * @return {String} Html table containing the latest trade volume data
	 */
	this.tradeVolumeData = function(data) {
		// create dynamic html for insertion into HTML DOM
		var html = "";
		// open table
		html += "<table class=\"table table-striped table-condensed\">";
		// create table headers
		html += "<tr><th>Currency</th><th>Total trades</th></tr>";
		// iterate each data element and create a table row
		for(var i=0; i<data.length;i++) {
			html += "<tr><td>" + data[i].currency + "</td><td>" + data[i].volume + "</td></tr>";
		}
		// close table
		html += "</table>";
		return html;
	};

	/**
	 * Produces a html table that contains the latest trade value data
	 *
	 * @method tradeValueData
	 * @return {String} Html table containing the latest trade value data
	 */
	this.tradeValueData = function(data) {
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
		for(var i=0; i<sortedArray.length;i++) {
			html += "<tr><td>" + sortedArray[i].currency + "</td><td>$" + numberWithCommas(Number(sortedArray[i].value).toFixed(2)) + "</td></tr>";
		}
		// close table
		html += "</table>";
		return html;
	};
}

// internal function to select the return response based on event type
function response(data) {
	switch (data.event) {
		case "CURRENCIES_SOLD_VOLUME":
			return { event: data.event, elementId: data.elementId, data: RespondWith.tradeVolumeData(data.data) };
		case "CURRENCIES_SOLD_VALUE":
			return { event: data.event, elementId: data.elementId, data: RespondWith.tradeValueData(data.data) };
		case "CURRENCIES_BOUGHT_VOLUME":
			return { event: data.event, elementId: data.elementId, data: RespondWith.tradeVolumeData(data.data) };
		case "CURRENCIES_BOUGHT_VALUE":
			return { event: data.event, elementId: data.elementId, data: RespondWith.tradeValueData(data.data) };
		case "COUNTRIES_VOLUME":
			return { event: data.event, elementId: data.elementId, data: RespondWith.countryVolumeData(data.data) };
		case "CURRENCY_PAIRS":
			return { event: data.event, elementId: data.elementId, data: RespondWith.currencyPairsData(data.data) };
		default:
			break;
	}
}

// internal function to reset the message buffer, count and function interval to default values
function resetWorker() {
	MESSAGE_BUFFER = "";
	COUNT = 0;
	if(typeof INTERVAL_ID !== "undefined") {
		clearInterval(INTERVAL_ID);
		INTERVAL_ID = undefined;
	}
}

// handle the web worker on message event
self.addEventListener('message', function(e) {
  // if we have reached the maximum buffer size then
  // return a response to the main thread and reset the worker
  if(COUNT > MAX_SIZE){
  	self.postMessage(response(e.data));
  	resetWorker();
  }

  // if the message count is 0, set the start time
  if(COUNT == 0){
  	START_TIME = new Date().getTime();
  }

  	// add the message data to the message buffer
	MESSAGE_BUFFER = e.data;
	// increment the count
	++COUNT;

	// set an interval functon to check if the maximum wait time has expired.
	// if the maximum wait time has elasped then return a response to the 
	// main thread and reset the worker 
	if('undefined' === typeof INTERVAL_ID) {
		INTERVAL_ID = setInterval(function () {
				if (new Date().getTime() - START_TIME > MAX_TIME) {
					self.postMessage(response(MESSAGE_BUFFER));
					resetWorker();
				}

			}
			, 1000);
	}
}, false);