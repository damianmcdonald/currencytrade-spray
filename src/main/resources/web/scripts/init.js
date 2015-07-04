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
 * Fires on page load and does the following:
 * * registers UI events
 * * registers a web socket or long polling connection depending on browser support
 * * shows the {waitingDialog} loading modal
 * * retrieves the data required for page load
 */
$(document).ready(function() {

    // establish handler for intra-page navigation and scroll animations
    $('a[href*=#]').each(function() {
        if ( location.pathname.replace(/^\//,'') == this.pathname.replace(/^\//,'')
            && location.hostname == this.hostname && this.hash.replace(/#/,'') ) {
            var targetId = $(this.hash), targetAnchor = $('[name=' + this.hash.slice(1) +']');
            var target = targetId.length ? targetId : targetAnchor.length ? targetAnchor : false;

            if (target) {
                var targetOffset = target.offset().top;
                $(this).click(function() {
                    $('html, body').animate( { scrollTop: targetOffset }, 1000 );
                    return false;
                });
            }
        }
    });

    // determine if web sockets and web workers are supported by the browser
    var browserSupported = function() {
        if (('function' !== typeof(WebSocket) && 'function' !== typeof(MozWebSocket))
                || 'undefined' === typeof(Worker))
        {
            log("Websockets and web workers are not supported!");
            return false;
        }
        return true;
    };

    // show the loading dialog modal
    waitingDialog.show("Retrieving trade data ....", browserSupported());

    // hide the alert div that is displayed when the websocket is disconnected
    $(".alert").alert();

    // retrieve the data required to load the page.
    // the timing of the loading events are staggered
    // to improve the UX and not to flood the server with requests.
    // we need to make sure that the ajax requests are not cached;
    // cache: false; otherwise Internet Explorer will not retieve
    // the latest data

    // currency pairs by sales volume
    setTimeout(function() {
        $.ajax({
            type: "GET",
            url: Api.URL_CURRENCYPAIR,
            cache: false,
            success: function (data) {
                UiUtils.updateCurrencyPairs(data.data, true, false);
            }, 
            dataType: "json"
        });
    }, 250);

    // currency sale volume by country
    setTimeout(function() {
        $ .ajax({
            type: "GET",
            url: Api.URL_COUNTRIESVOLUME,
            cache: false, 
            success: function (data) {
                UiUtils.updateCountriesVolume(data.data, true, false);
            }, 
            dataType: "json"
        });
    }, 500);

    // currencies sold by volume
    setTimeout(function() {
        $ .ajax({
            type: "GET",
            url: Api.URL_SELLVOLUME,
            cache: false,
            success: function (data) {
                UiUtils.updateTradeVolume(data.data, "sell-volume", true, false);
            }, 
            dataType: "json"
        });
    }, 750);

    // currencies sold by value
    setTimeout(function() {
        $ .ajax({
            type: "GET",
            url: Api.URL_SELLVALUE,
            cache: false, 
            success: function (data) {
                UiUtils.updateTradeValue(data.data, "sell-value", true, false);
            }, 
            dataType: "json"
        });
    }, 1000);

    // currencies bought by volume
    setTimeout(function() {
        $ .ajax({
            type: "GET",
            url: Api.URL_BUYVOLUME,
            cache: false, 
            success: function (data) {
                UiUtils.updateTradeVolume(data.data, "buy-volume", true, false);
            }, 
            dataType: "json"
        });
    }, 1250);

    // currencies bought by value
    setTimeout(function() {
        $ .ajax({
            type: "GET",
            url: Api.URL_BUYVALUE,
            cache: false,  
            success: function (data) {
                UiUtils.updateTradeVolume(data.data, "buy-value", true, false);
            }, 
            dataType: "json"
        });
    }, 1500);

    // country codes for countries from which a trade has been placed
    setTimeout(function() {
        $ .ajax({
            type: "GET",
            url: Api.URL_COUNTRYCODES,
            cache: false,
            success: function (data) {
                UiUtils.updateCountriesInfo(data.data, true, true);
                setTimeout(function() { LongPolling.activateMapUpdates(); }, 5000);
            }, 
            dataType: "json"
        });
    }, 1750);

    // register a websocket connection if web sockets are supported by the browser
    // otherwise use long polling
    if(browserSupported()) {
        WebSockets.registerWebSockets();
        WebWorkers.registerWebWorkers();
    } else {
        LongPolling.activateLongPolling();
    }

    // register UI event handlers

    $( "#random-post-btn" ).click(function() {
        UiUtils.postRandomTradeData();
    });

    $( "#bulk-post-btn" ).click(function() {
        UiUtils.postBulkTradesData();
    });

    $( "#map-nav" ).click(function() {
        UiUtils.recaclculateOffsets();
    });

})