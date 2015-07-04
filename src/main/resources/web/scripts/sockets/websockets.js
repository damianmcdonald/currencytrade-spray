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
 * Provides web sockets handlers for browsers that support web sockets
 *
 * @class WebSockets
 * @static
 */
var WebSockets = new function() {

    /**
     * Registers handlers for web socket events
     *
     * @method registerWebSockets
     * @return {Unit}
     */
    this.registerWebSockets = function() {
        // open a WebSocket
        var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
        // connect to the web socket port on the server
        var mapSocket = new WS("ws://" + window.location.hostname + ":6696/v1/ws")

        // handle the onmessage web socket event
        mapSocket.onmessage = function (event) {
            // the event data
            var data = event.data
            // the data parsed into json
            var json = $.parseJSON(data);

            // choose the method to execute based on event type
            switch (json.event) {
                case "SOCKET_OPENED":
                    log("Web socket opened");
                    break;
                case "TRADE_PERSISTED":
                    WebWorkers.getLatestTradesWorker().postMessage(json.data);
                    break;
                case "CURRENCIES_SOLD_VOLUME":
                    WebWorkers.getSoldVolumeWorker().postMessage({ "event": "CURRENCIES_SOLD_VOLUME", "elementId": "sell-volume", "data": json.data });
                    break;
                case "CURRENCIES_SOLD_VALUE":
                    WebWorkers.getSoldValueWorker().postMessage( { "event": "CURRENCIES_SOLD_VALUE", "elementId": "sell-value", "data": json.data } );
                    break;
                case "CURRENCIES_BOUGHT_VOLUME":
                    WebWorkers.getBoughtVolumeWorker().postMessage({ "event": "CURRENCIES_BOUGHT_VOLUME", "elementId": "buy-volume", "data": json.data });
                    break;
                case "CURRENCIES_BOUGHT_VALUE":
                    WebWorkers.getBoughtValueWorker().postMessage({ "event": "CURRENCIES_BOUGHT_VALUE", "elementId": "buy-value", "data": json.data });
                    break;
                case "COUNTRIES_VOLUME":
                    WebWorkers.getCountryVolumeWorker().postMessage( { "event": "COUNTRIES_VOLUME", "elementId": "", "data": json.data } );
                    break;
                case "CURRENCY_PAIRS":
                    WebWorkers.getCurrencyPairsWorker().postMessage( { "event": "CURRENCY_PAIRS", "elementId": "", "data": json.data } );
                    break;
                default:
            }
        }

        // handle websocket errors
        var onalert = function (event) {
            $(".alert").removeClass("hide")
            log("websocket connection closed or lost")
        }
        // register the error handlers with the error events
        mapSocket.onerror = onalert
        mapSocket.onclose = onalert
    };
}
