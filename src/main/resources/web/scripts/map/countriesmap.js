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
 * Provides functions to create a Google Map, add markers to the map
 * and resposition the map
 *
 * @class CountriesMap
 * @static
 */
var CountriesMap = new function () {
    /**
     * Global reference to the Google Map object {google.maps.Map}
     *
     * @property MAP
     */
    var MAP;

    /**
     * Contains the Marker objects {google.maps.Marker} that have been added to the
     * Google Map object {google.maps.Map}
     *
     * @property MARKERS
     * @final
     */
    var MARKERS = new Array();

    /**
     * Contains the LatLng objects {google.maps.LatLng} that have been added to the
     * Google Map object {google.maps.Map}
     *
     * @property LATITUDES_LONGITUDES
     * @final
     */
    var LATITUDES_LONGITUDES = new Array();

    /**
     * A JSEL (XPath implementation for JSON) representation of
     * static data relating to countries
     *
     * @property COUNTRIES_DOM
     * @final
     */
    var COUNTRIES_DOM = jsel(CountriesData.COUNTRIES);

    /**
     * A JSEL (XPath implementation for JSON) representation of
     * static data relating to continents
     *
     * @property CONTINENTS_DOM
     * @final
     */
    var CONTINENTS_DOM = jsel(CountriesData.CONTINENTS);

    /**
     * A JSEL (XPath implementation for JSON) representation of
     * static data relating to countries longitute and
     * latitude coordinates
     *
     * @property COORDS_DOM
     * @final
     */
    var COORDS_DOM = jsel(CountriesData.COORDINATES);

    /**
     * Retrieves the LatLng objects {google.maps.LatLng} that have been added to the
     * Google Map object {google.maps.Map}
     *
     * @method getLatLngCoords
     * @return {Array[google.maps.LatLng]} the array of google.maps.LatLng coordinates
     */
    this.getLatLngCoords = LATITUDES_LONGITUDES;

    /**
     * Retrieves information about a country based on the ISO 3166-1 alpha-2,
     * 2 letter country codes
     *
     * getCountryInfoByCode("AU"); // Australia
     * getCountryInfoByCode("TR"); // Turkey
     *
     * [ISO 3166-1 alpha-2 country codes](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)
     *
     * @method getCountryInfoByCode
     * @param code {String} ISO 3166-1 alpha-2, 2 letter country code
     * @return {Unit}
     */
    this.getCountryInfoByCode = function(code) {

        // if we have already registered a map marker for this country
        // then exit early as there is no need to proceed
        if ($.inArray(code, MARKERS) >= 0) {
            return false;
        }

        // find the country, using JSEL XPath, by country code
        var country = COUNTRIES_DOM.select("//countries/" + code);

        // exit if we could not find a country - not ALL countries are contained
        // within the COUNTRIES_INFO data
        if (!country) {
            log("country not found: " + code);
            return false;
        }

        // find the latitude and longitude coordinates, using JSEL XPath, by country code
        var coords = COORDS_DOM.select("//countries/" + code.toLowerCase());

        // exit if we could not find latitude and longitude coordinates - not ALL coordinates
        // are contained within the COORDS_INFO data
        if (!coords) {
            console.log("coords not found for: " + code);
            return false;
        }

        // format the longitutde and latitude into the format required for google.maps.LatLng
        var lat = $.number(coords.lat, 4, '.');
        var lng = $.number(coords.long, 4, '.');

        var latlng = new google.maps.LatLng(lat, lng);

        // create a new map marker for the country
        createNewMarker(country, latlng);

        // add the country code to the MARKERS array
        MARKERS.push(code);
    };

    /**
     * Create a new Map Marker {google.maps.Marker}
     *
     * var countryMarker =  {
     *       AD": {
     *          "name": "Andorra",
     *          "native": "Andorra",
     *          "phone": "376",
     *          "continent": "EU",
     *          "capital": "Andorra la Vella",
     *          "currency": "EUR",
     *          "languages": "ca"
     *       }
     *   }
     *
     * var coords = new google.maps.LatLng(-35.308000, 149.124500);
     *
     * createNewMarker(countryMarker, coords);
     *
     * @method createNewMarker
     * @param countryMarker {Object}
     * @param coords {google.maps.LatLng}
     * @return {Unit}
     */
    function createNewMarker(countryMarker, coords) {

        // add the coords to the LATITUDES_LONGITUDES array
        LATITUDES_LONGITUDES.push(coords);

        // find the continent, using JSEL XPath, by country.continent
        var continent = CONTINENTS_DOM.select("//continents/" + countryMarker.continent);

        // create dynamic html for insertion into HTML DOM
        var html = "";

        // open outer div
        html += "<div style=\"color: #306d9f\">";
        // h3 title
        html += "<h3>" + countryMarker.name + "</h3>";
        // open inner div
        html += "<div>";
        // open table
        html += "<table class=\"table table-stripped\">";

        // open table rows
        html += "<tr><td>Continent</td><td>" + continent.name + "</td></tr>";
        html += "<tr><td>Native</td><td>" + countryMarker.native + "</td></tr>";
        html += "<tr><td>Capital</td><td>" + countryMarker.capital + "</td></tr>";
        html += "<tr><td>Languages</td><td>" + countryMarker.languages + "</td></tr>";
        html += "<tr><td>Currency</td><td>" + countryMarker.currency + "</td></tr>";
        html += "<tr><td>Intl Prefix</td><td>" + countryMarker.phone + "</td></tr>";
        // close table rows

        // close table
        html += "</table>";
        // close inner div
        html += "</div>";
        // close outer div
        html += "</div>";

        // create map marker
        var marker = new google.maps.Marker({
            position: coords,
            map: MAP,
            title: countryMarker.name
        });

        // create an infowindow which will be opened when the map marker is clicked
        var infowindow = new google.maps.InfoWindow({
            content: html,
            maxWidth: 350
        });

        // add the event listerner to handle the marker click event which will open the infowindow
        google.maps.event.addListener(marker, 'click', function () {
            infowindow.open(MAP, marker);
        });
    };

    /**
     * Fits the Map {google.maps.Map} to the boundaries of the map Markers {google.maps.Marker}.
     * This Map canvas is repositioned to display all the map Markers.
     *
     * @method fitMapToBounds
     * @return {Unit}
     */
    this.fitMapToBounds = function() {
        // create a new viewpoint bound
        var bounds = new google.maps.LatLngBounds();
        // iterate the longitude and latitude coords of each marker on the map
        for (var i = 0; i < LATITUDES_LONGITUDES.length; i++) {
            // increase the bounds for each marker coords
            bounds.extend(LATITUDES_LONGITUDES[i]);
        }
        // fit these bounds to the map
        MAP.fitBounds(bounds);
        // set a desied zoom level
        MAP.setZoom(3);
    };

    /**
     * Create an instance of a Google Map object {google.maps.Map}
     *
     * @return {Unit}
     */
    this.initializeMap = function() {
        // set initial zoom level
        var mapOptions = {
            scrollwheel: false,
            navigationControl: true,
            mapTypeControl: true,
            scaleControl: true,
            draggable: true,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            zoom: 3
        };
        // initialise google map
        MAP = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
    };

}