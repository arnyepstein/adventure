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
	var showLine = function(elclass, message) {
		var el = '<p class="' + elclass + '">' + message + "</p>";
		$("#console").append($(el));
	}
	// -----------------------------------------------------------------
	var log = function(message) {
		var console = document.getElementById('console');
		var p = document.createElement('p');
		p.style.wordWrap = 'break-word';
		p.appendChild(document.createTextNode(message));
		console.appendChild(p);
		while (console.childNodes.length > 25) {
			console.removeChild(console.firstChild);
		}
		console.scrollTop = console.scrollHeight;
	}
	// -----------------------------------------------------------------
	var sendCommand = function(message) {
		showLine("userInput", message);

		$ae.doRequest({
			method: "POST",
			url: ECHO_URL,
			data: JSON.stringify({ request: message }),
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

		// //$( "p" ).text( "The DOM is now loaded and can be manipulated." );
		// ACID_TOKEN = $ae.Local.get("acid");
		// var form = $("#adminLoginForm");
		// var prog = $("#verifyTokenProgress");
		// // Check to see if we're still logged on
		// // if(mockNet.inUse) {
		// //     var resp = mockNet.login();
		// //     $ae.hide(prog);
		// //     $ae.show(form);
		// //     GW.onLogin(resp);
		// //     return;
		// // }
		// if(ACID_TOKEN) {
		// 	$ae.hide(form);
		// 	$ae.show(prog);
		// 	getRequest(
		// 		ADMIN_BASE_URL+"/userinfo",
		// 		function (resp) {
		// 			$ae.hide(prog);
		// 			$ae.show(form);
		// 			$ae.hide(".whenUnknown");
		// 			$ae.show(".whenKnown");
		// 			USER_INFO = resp;
		// 			APP.onLogin(resp.attributes);
		// 		}
		// 	);
		// } else {
		// 	window.location.pathname = ROOT_URL + "/admin";
		// 	$ae.show(form);
		// }
	});

	$ae.onAppLoaded(function() {
		// $.ajaxSetup({
		// 	beforeSend: function(xhr) {
		// 		if(ACID_TOKEN) {
		// 			xhr.setRequestHeader(ACID_HEADER_NAME, ACID_TOKEN);
		// 		}
		// 	}
		// });

		// Install Event Handlers
		// --------------------------------------------------------------
		// // Admin Login Form:
		// // Handle Form Submit
		// GW.formSubmitter(
		//     "#adminLoginForm",
		//     ADMIN_LOGIN_URL,
		//     {
		//         okfunc: GW.onLogin,
		//         failfunc: function() { $ae.Local.remove("authToken"); }
		//     }
		// );
		// --------------------------------------------------------------
		$( "#echo" ).click(function( event ) {
			event.preventDefault();
			event.stopPropagation();
			sendCommand($("#message").val());
		});

		$("#message").keypress(
			function(event) {
				var input = $("#message");
				if(event.keyCode === 13){
					sendCommand(input.val());
					input.val("");
				}
				return true;
			}
		);

	});

	// -----------------------------------------------------------------
	window.APP = APP;
}).call(this);