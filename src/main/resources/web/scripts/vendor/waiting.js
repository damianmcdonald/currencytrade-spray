/**
 * Module for displaying "Waiting for..." dialog using Bootstrap
 *
 * @author Eugene Maslovich <ehpc@em42.ru>
 */

var waitingDialog = (function ($) {

    // Creating modal dialog's DOM
	var $dialog = $(
		'<div class="modal fade" data-backdrop="static" data-keyboard="false" tabindex="-1" role="dialog" aria-hidden="true" style="padding-top:15%; overflow-y:visible;">' +
			'<div class="modal-dialog modal-m">' +
				'<div class="modal-content">' +
					'<div class="modal-header">'+
						'<h3 style="margin:0;"></h3>'+
					'</div>' +
					'<div class="modal-info alert">'+
					'</div>' +
					'<div class="modal-body">' +
						'<div class="progress active" style="margin-bottom:0;">'+
							'<div id="loading-progress-bar" class="progress-bar" role="progress-bar" style="width: 0%"></div>'+
						'</div>' +
					'</div>' +
				'</div>'+
			'</div>'+
		'</div>');

	return {
		/**
		 * Opens our dialog
		 * @param message Custom message
		 * @param options Custom options:
		 * 				  options.dialogSize - bootstrap postfix for dialog size, e.g. "sm", "m";
		 * 				  options.progressType - bootstrap postfix for progress bar type, e.g. "success", "warning".
		 */
		show: function (message, browserSupported, options) {
			// Assigning defaults
			var settings = $.extend({
				dialogSize: 'm',
				progressType: ''
			}, options);
			if (typeof message === 'undefined') {
				message = 'Loading';
			}
			if (browserSupported) {
				$dialog.find('.modal-info').addClass("alert-info");
				$dialog.find('.modal-header').css("color", "#306d9f");
				$dialog.find('.modal-info').css("color", "#306d9f");
				$dialog.find('.progress').css("color", "#306d9f");
				$dialog.find('.modal-info').html("<p><strong>Great news!</strong> Your browser suppots websockets which will provide you with the best user experience possible.</p>");
			} else {
				$dialog.find('.modal-info').addClass("alert-warning");
				$dialog.find('.modal-info').html("<p><strong>Oh dear!</strong> Your browser does not suppot websockets. We will switch you to long polling which is a slighty degraded user experience.</p><p>We recommend upgading to a browser that support web sockets.</p>");
				$dialog.find('.modal-header').css("color", "orange");
				$dialog.find('#loading-progress-bar').addClass("progress-bar-warning");
			}
			if (typeof options === 'undefined') {
				options = {};
			}
			// Configuring dialog
			$dialog.find('.modal-dialog').attr('class', 'modal-dialog').addClass('modal-' + settings.dialogSize);
			$dialog.find('#loading-progress-bar').attr('aria-valuenow', '0');
			$dialog.find('#loading-progress-bar').attr('aria-valuemin', '0');
			$dialog.find('#loading-progress-bar').attr('aria-valuemax', '100');
			if (settings.progressType) {
				$dialog.find('#loading-progress-bar').addClass('progress-bar-' + settings.progressType);
			}
			$dialog.find('h3').text(message);
			// Opening dialog
			$dialog.modal();
		},
		/**
		 * Closes dialog
		 */
		hide: function () {
			$dialog.modal('hide');
		}
	}

})(jQuery);
