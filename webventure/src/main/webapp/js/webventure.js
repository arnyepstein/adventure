(function() {

	var APP = {};

	// -----------------------------------------------------------------
	var ROOT_URL = window.location.pathname;
	ROOT_URL = ROOT_URL.substring(0, ROOT_URL.lastIndexOf('/'));
	var ECHO_URL = ROOT_URL + "/api/echo";
	// -----------------------------------------------------------------
	var execRequest = function (method, url, data, onSuccess, onFailure) {
		return $ae.doRequest({
			ctx:APP,
			url: url,
			data: data,
			type: method,
			success: $ae.emptyFunc,
			onSuccess: onSuccess,
			onFailFunc: onFailure | APP.showRequestError
		});
	}
	// -----------------------------------------------------------------
	var getRequest = function (url, onSuccess, onFailure) {
		return execRequest("GET", url, null, onSuccess, onFailure);
	}
	// -----------------------------------------------------------------
	var postRequest = function (url, data, onSuccess, onFailure) {
		return execRequest("POST", url, data, onSuccess, onFailure);
	}
	// -----------------------------------------------------------------
	var deleteRequest = function (url, onSuccess, onFailure) {
		return execRequest("DELETE", url, null, onSuccess, onFailure);
	}

	// -----------------------------------------------------------------
	APP.buildErrorMessage = function(resp){
		var info = resp.responseJSON;
		var msg = "Request error: " + resp.statusText + "(" + resp.status + ")</br>";
		if(info) {
			msg += "What: " + info.message + "</br>";
			if (info.cause) {
				msg += "Why: " + info.cause + "</br>";
			}
		}
		return msg;
	}
	// -----------------------------------------------------------------
	APP.showRequestError = function (resp) {
		alert(APP.buildErrorMessage(resp));
	};
	// -----------------------------------------------------------------
	APP.showFailure = function (el, resp) {
		var msg = APP.buildErrorMessage(resp);
		if(el) {
			el.html(msg);
		} else {
			alert(msg);
		}
	};
	// -----------------------------------------------------------------
	$ae.showSubmitterFailure = function (el, resp) {
		el.html("Failure: " + resp.statusText + "(" + resp.status + ")");
	};




	// -----------------------------------------------------------------
	var consoleEl = null;
	var inputEl = null;
	// -----------------------------------------------------------------
	var newLineEl = function(elclass, message) {
		return $('<p class="' + elclass + '">' + message + "</p>");
	}
	// -----------------------------------------------------------------
	var showLine = function(elclass, message) {
		if(message == null)
			return;
		inputEl.before(newLineEl(elclass, message));
		var contentHeight = consoleEl[0].scrollHeight;
		var ctlHeight = consoleEl.outerHeight();
		consoleEl.scrollTop(contentHeight - ctlHeight);
	}
	// -----------------------------------------------------------------
	var clearScreen = function() {
		consoleEl.empty();
		inputEl = newLineEl("userInput", "_");
		consoleEl.append(inputEl);
	}
	// -----------------------------------------------------------------
	var curText = "";

	var cursor = "_";
	// -----------------------------------------------------------------
	var setInputElText = function(text) {
		inputEl.text(text + cursor);

	}
	// -----------------------------------------------------------------
	var onBlink = function() {
		cursor = (cursor == "_") ? " " : "_";
		setInputElText(curText)
	}
	// -----------------------------------------------------------------
	var onKey = function(event) {
		var code = event.keyCode;
		if( event.metaKey || event.ctrlKey || event.altKey ) {
			return;
		}
		if(code == 13) {
			sendCommand(curText);
			curText = "";
			setInputElText(curText)
			return;
		}
		var len = curText.length;
		var newText = "";
		if(code == 8) {
			if(len == 0)  return;
			newText = curText.substring(0, len-1);
		} else if(code >= 0x41 && code <= 0x5A ) {
			if(! event.shiftKey) code += 0x20;
			newText = curText + String.fromCharCode(code);
		} else if(code == 0x20 || (code >= 0x30 && code <= 0x39) ) {
			newText = curText + String.fromCharCode(code);
		} else {
			return;
		}
		curText = newText;
		setInputElText(curText)
	}
	// -----------------------------------------------------------------
	var sendCommand = function(message) {
		if(message == null) {
			clearScreen();
		} else {
			showLine("userInput", message);
		}

		var body = { command: message==null ? "new" : "move", message: message };

		$ae.doRequest({
			method: "POST",
			url: ECHO_URL,
			data: JSON.stringify(body),
			onSuccess: function(resp) {
				_.each(
					resp.data,
					function(msg) {
						showLine("gameText", msg);

						// log(msg);
					}
				);
			}
		});
	}
	// -----------------------------------------------------------------
	$ae.appMain(function() {
		consoleEl = $("#console");
		clearScreen();
	});

	$ae.onAppLoaded(function() {

		$( "#newGame" ).click(function( event ) {
			event.preventDefault();
			event.stopPropagation();
			sendCommand(null);
		});

		// $("#message").keypress(
		// 	function(event) {
		// 		var input = $("#message");
		// 		if(event.keyCode === 13){
		// 			sendCommand(input.val());
		// 			input.val("");
		// 		}
		// 		return true;
		// 	}
		// );
		// $(window).keypress(
		// 	function(event) {
		// 		event.preventDefault();
		// 		event.stopPropagation();
		// 		var code = event.keyCode;
		// 		onKey(code);
		// 	}
		// );
		$(window).keydown(
			function(event) {
				event.preventDefault();
				event.stopPropagation();
				onKey(event);
			}
		);

		window.setInterval(onBlink, 600);

	});

	// -----------------------------------------------------------------
	window.APP = APP;
}).call(this);