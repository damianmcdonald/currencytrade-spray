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
var MAX_TIME = 500;

/**
 * Used to store the latest message updates
 *
 * @property MESSAGE_BUFFER
 */
var MESSAGE_BUFFER = "";

/**
 * Used to store the time at which the first message was received
 *
 * @property START_TIME
 */
var START_TIME;

/**
 * Used to store the current count of updates
 *
 * @property COUNT
 */
var COUNT = 0;

/**
 * Holds a reference following a call to {setInterval} in order to track
 * the interval function
 *
 * @property INTERVAL_ID
 */
var INTERVAL_ID;

// internal function to reset the message buffer, count and function interval to default values
function resetWorker() {
	MESSAGE_BUFFER = "";
	COUNT = 0;
	if(typeof INTERVAL_ID !== "undefined") {
		clearInterval(INTERVAL_ID);
		INTERVAL_ID = undefined;
	}
}

// internal function to generate a html table row representing the latest trade data
function generateHtml(data) {
	// create dynamic html for insertion into HTML DOM
	var html = "";

	// open a new table row
	html += "<tr>";
	// create table data
	html += "<td>" + data.currencyFrom + "</td>";
	html += "<td>" + data.amountSell + "</td>";
	html += "<td>" + data.currencyTo + "</td>";
	html += "<td>" + data.amountBuy + "</td>";
	html += "<td>" + data.rate + "</td>";
	html += "<td>" + data.timePlaced + "</td>";
	html += "<td>" + data.originatingCountry + "</td>";
	// close the table row
	html += "</tr>";
	return html;
}

// handle the web worker on message event
self.addEventListener('message', function(e) {
	// if we have reached the maximum buffer size then
  	// return a response to the main thread and reset the worker
	  if(COUNT > MAX_SIZE){
		  self.postMessage(MESSAGE_BUFFER);
		  resetWorker();
	  }

	// if the message count is 0, set the start time
	if(COUNT == 0){
		START_TIME = new Date().getTime();
	}

	// add the message data to the message buffer
	MESSAGE_BUFFER += generateHtml(e.data);
	// increment the count
	++COUNT;

	// set an interval functon to check if the maximum wait time has expired.
	// if the maximum wait time has elasped then return a response to the 
	// main thread and reset the worker 
	if(typeof INTERVAL_ID === "undefined") {
		INTERVAL_ID = setInterval(function() {
	    if ( new Date().getTime() - START_TIME > MAX_TIME ) {
	        self.postMessage(MESSAGE_BUFFER);
			resetWorker();
		}
		}, 1000);
	}
}, false);