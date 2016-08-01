(function() {

	var APP = {};

	// -----------------------------------------------------------------
	var ROOT_URL = window.location.pathname;
	ROOT_URL = ROOT_URL.substring(0, ROOT_URL.lastIndexOf('/'));
	var ECHO_URL = ROOT_URL + "/api/echo";
	var INPUT_URL = ROOT_URL + "/game/input";
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
	var gameContainerEl = null;
	var consoleEl = null;
	var inputEl = null;
	// -----------------------------------------------------------------
	var newLineEl = function(elclass, message) {
		return $('<p class="' + elclass + '">' + message + "</p>");
	}
	// -----------------------------------------------------------------
	var appendLineEl = function(el) {
		inputEl.before(el);
		var contentHeight = consoleEl[0].scrollHeight;
		var ctlHeight = consoleEl.outerHeight();
		consoleEl.scrollTop(contentHeight - ctlHeight);
	}
	// -----------------------------------------------------------------
	var showLine = function(elclass, message) {
		if(message != null) {
			appendLineEl(newLineEl(elclass, message))
		}
	}
	// -----------------------------------------------------------------
	var showGameLine = function(message) {
		if(message != null) {
			appendLineEl(newLineEl("gameText", message))
		}
	}

	var showInputLine = function(message) {
		appendLineEl(newLineEl("userInput", message))
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
	var onTimerEvent = function() {
		if(mode && mode.onTimerEvent) {
			mode.onTimerEvent()
		}
	}
	// -----------------------------------------------------------------
	var onKey = function(event) {
		if(! mode.onCommandEntered) {
			return;
		}
		var code = event.keyCode;
		if( event.metaKey || event.ctrlKey || event.altKey ) {
			return;
		}
		if(code == 13) {
			if(mode.onCommandEntered) {
				mode.onCommandEntered(curText)
			}
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
			showInputLine(message);
		}

		var body = { command: message==null ? "new" : "move", message: message };

		$ae.doRequest({
			method: "POST",
			url: INPUT_URL,
			data: JSON.stringify(body),
			onSuccess: function(resp) {
				_.each(
					resp.data,
					function(msg) {
						showGameLine(msg);

						// log(msg);
					}
				);
			}
		});
	}
	// -----------------------------------------------------------------
	var onPlayButton = function(event) {
		event.preventDefault();
		event.stopPropagation();
		if(mode == "welcome") {
			setMode("game");
		} else {
		}
	}
	// -----------------------------------------------------------------
	var mode = "";
	var screenName = ""
	var localInfo = null;
	var currentPlayer = null;
	// -----------------------------------------------------------------
	var setMode = function(newMode) {
		// $ae.showOptional($("#mainContainer"), newMode);
		if(mode == newMode) return;
		if(mode.onEndMode) {
			mode.onEndMode();
		}
		mode = newMode;
		if(mode.onBeginMode) {
			mode.onBeginMode();
		}
		// if(mode == "welcome") {
		// 	clearScreen();
		// 	inputEl.text("");
		// 	showGameLine("Welcome to WebVenture - an online version of Colossal Cave Adventure")
		// 	showGameLine('Enter your Screen Name: ')
		// 	var screenForm = $('<form><input type="input" name="screenname" /></form>')
		// 	appendLineEl(screenForm)
		// 	screenForm.submit(onPlayButton);
		// }
		// if(mode == "game") {
		// 	clearScreen();
		// 	// Start the game
		// 	sendCommand(null);
		// }
	}
	// -----------------------------------------------------------------
	var updateInfo = function() {
		$ae.Local.setJson("adventure", localInfo)
	}
	// -----------------------------------------------------------------
	var WelcomeMode = {
		onBeginMode: function() {
			clearScreen();
			inputEl.text("");
			localInfo = $ae.Local.getJson("adventure")
			if(!localInfo) {
				localInfo =  { users:{} };
				updateInfo();
			}
			showGameLine("Welcome to WebVenture - an online version of Colossal Cave Adventure")
			var namecount = 0
			_.each(
				localInfo.users,
				function(val, key) {
					if(namecount == 0) {
						showGameLine("Select your Screen Name");
						namecount++;
					}
					appendLineEl(newLineEl("gameText", '&nbsp;&nbsp;&nbsp;<button name="' + key + '">I am ' + key + '</button>'))
				}
			);
			if(namecount > 0) {
				showGameLine('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;or')
			}
			showGameLine('Enter your Screen Name: ')
			// var screenForm = $('<form><input type="input" name="screenname" /></form>')
			// appendLineEl(screenForm)
			// screenForm.submit(onPlayButton);

			$ae.hide($("#newGame"))
		},

		onCommandEntered: function(command) {
			// process Screen Name
			screenName = command;
			curText = "";
			showInputLine(command);
			var body = {
				name: screenName
			}
			$ae.doRequest({
				method: "POST",
				url: ROOT_URL + "/game/user",
				data: JSON.stringify(body),
				onSuccess: function(resp) {
					if( "Added" == resp.status) {
						setMode(GameMode);
					} else {
						showGameLine('Sorry, that name is in use.  Try another name: ')
					}
				}
			});
		},

		onPlayButton: function(event) {
			setMode(GameMode);
		},

		onTimerEvent: function() {
			cursor = (cursor == "_") ? " " : "_";
			setInputElText(curText)
		}


	};

	// -----------------------------------------------------------------
	var GameMode = {
		onBeginMode: function() {
			clearScreen();
			if( ! localInfo.users[screenName] ) {
				currentPlayer = { name: screenName }
				localInfo.users[screenName] = currentPlayer;
				updateInfo();
			} else {
				currentPlayer = localInfo.users[screenName]
			}
			// Start the game
			sendCommand(null);
		},

		onPlayButton: function(event) {
		},

		onCommandEntered: function(command) {
			sendCommand(command);
			curText = "";
			setInputElText(command)
		},

		onTimerEvent: function() {
			cursor = (cursor == "_") ? " " : "_";
			setInputElText(curText)
		}
	}

	// -----------------------------------------------------------------
	$ae.appMain(function() {
		gameContainerEl = $(".game-container");
		consoleEl = gameContainerEl.find(".console");
		setMode(WelcomeMode);
		// clearScreen();
	});
	// -----------------------------------------------------------------
	$ae.onAppLoaded(function() {

		$( "#newGame" ).click(function( event ) {
			event.preventDefault();
			event.stopPropagation();
			if(mode.onPlayButton) {
				mode.onPlayButton(event);
			}
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

		window.setInterval(onTimerEvent, 600);

	});

	// -----------------------------------------------------------------
	window.APP = APP;
}).call(this);