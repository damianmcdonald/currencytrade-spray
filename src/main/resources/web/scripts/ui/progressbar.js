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
 * A utiltity that can updates a progress bar based on the percentage 
 * of task completions
 *
 * @class ProgressBar
 * @static
 */
var ProgressBar = new function() {

    /**
     * Defines the number of data loading events that need to be completed
     * in order to consider the page data fully loaded .
     *
     * @property EXPECTED_COMPLETIONS
     * @final
     */
    var EXPECTED_COMPLETIONS = 7;

    /**
     * Used to keep track of the number of data loading events that have completed.
     *
     * @property EXPECTED_COMPLETIONS
     */
    var CURRENT_COMPLETIONS = 0;

    /**
     * Updates a progress bar based on the percentage of task completions
     *
     * @method update
     * @return {Unit}
     */
    this.update = function() {
        // update the progress bar each time a data loading event completes
        switch(CURRENT_COMPLETIONS) {
            case 1:
                $("#loading-progress-bar").attr('style', 'width: 14%;');
                $("#loading-progress-bar").text("14%");
                break;
            case 2:
                $("#loading-progress-bar").attr('style', 'width: 28%;');
                $("#loading-progress-bar").text("28%");
                break;
            case 3:
                $("#loading-progress-bar").attr('style', 'width: 42%;');
                $("#loading-progress-bar").text("42%");
                break;
            case 4:
                $("#loading-progress-bar").attr('style', 'width: 56%;');
                $("#loading-progress-bar").text("56%");
                break;
            case 5:
                $("#loading-progress-bar").attr('style', 'width: 70%;');
                $("#loading-progress-bar").text("70%");
                break;
            case 6:
                $("#loading-progress-bar").attr('style', 'width: 84%;');
                $("#loading-progress-bar").text("84%");
                break;
            case 7:
                $("#loading-progress-bar").attr('style', 'width: 100%;');
                $("#loading-progress-bar").text("100%");
                break;
            default:
            // do nothing
        }
        // increment CURRENT_COMPLETIONS
        ++CURRENT_COMPLETIONS;

        // execute when we have reached the expected number of completions
        if(CURRENT_COMPLETIONS == EXPECTED_COMPLETIONS) {
            // delay the close of the waitingDialog modal to improve the UX
            setTimeout( function(){ waitingDialog.hide(); }, 1000 );
        }
    };

}
