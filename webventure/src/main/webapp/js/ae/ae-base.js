(function() {

	var THIS = {};

	// -----------------------------------------------------------------
	var emptyFunc =  function () {}


	// This function runs when the page is "ready" according to JQuery
	// It will invoke all functions registered at 'onAppLoaded', and then it will
	// Invoke the registered "appMain" function.
	var startupList = [];
	var _appMain = emptyFunc;
	var startApp = function() {
		// Run each of the startup functions
		_.each(startupList,
			function (fn, index, list) {
				fn();
			}
		);
		_appMain();
	}
	// Register this with JQuery
	$(startApp);

	/**
	 * Called by each package to register code that is run when the App (page) is fully loaded
	 * @param fn
	 */
	THIS.onAppLoaded = function(fn) {
		startupList.push(fn);
	}

	var myTemplate = function () {/*
	 <div id="someId">
	 some content<br />
	 <a href="#someRef">someRefTxt</a>
	 </div>
	 */};

	var fn2str = THIS.fn2str = function (fn) {
		var str = fn.toString();
		var arr = fn.toString().match(/[^]*\/\*([^]*)\*\/\}$/);
		var ans = arr[1];
		var ans1 = ans.trim();
		return ans1;
	}

	/**
	 * Called to register the main App entry point after the page is ready.
	 * @param fn
	 */
	THIS.appMain = function(fn) {
		_appMain = fn;
		// var x = fn2str(myTemplate);
		// var c1 = x[0];
	}

	/**
	 * Returns the value, invoking a function as necessary
	 * @param val Either a value object or a function that will produce the value
	 * @returns {*}
	 */
	var getPropertyValue = THIS.getPropertyValue = function(val) {
		if(_.isFunction(val)) {
			val = val();
		}
		return val;
	}

	// -----------------------------------------------------------------
	THIS.emptyFunc = emptyFunc;
	// -----------------------------------------------------------------
	THIS.getSelector = function(val) {
		val = $( getPropertyValue(val) );
		return val;
	}

	// ----------------c-------------------------------------------------
	THIS.hide = function (el) {
		$(el).addClass("hidden");
	};
	// -----------------------------------------------------------------
	THIS.show = function(el) {
		$(el).removeClass("hidden");
	};
	// -----------------------------------------------------------------
	THIS.getFormData = function (el) {
		var values = el.serializeArray();
		var o = {};
		$.each(values, function () {
			if (o[this.name]) {
				if (!o[this.name].push) {
					o[this.name] = [o[this.name]];
				}
				o[this.name].push(this.value || '');
			} else {
				o[this.name] = this.value || '';
			}
		});
		return o;
	};
	// -----------------------------------------------------------------
	THIS.defaultRequestError = emptyFunc;
	// -----------------------------------------------------------------
	var callbackCtx = null;
	THIS.setCallbackCtx = function(ctx) {
		callbackCtx = ctx;
	}
	// --------------------------------------------------------------
	// Makes an HTTP request based on specified options.  Returns a promise
	// (with registered done and fail handlers as specified)
	// Options:
	//  url - The request URL
	//  method - The HTTP method to use
	//  data - The data for the request body (default: null)
	//  datatype (jquery-style) - (default "json") -
	// contentType - The MIME type for the response (default application/json;charset=ISO-8859-1)
	//  ctx - (optional) Object that becomes 'this' for calling onSuccess and onFailure
	//  onSuccess  - called when a 2xx is received and the resp.status is "success"
	//  onFailure - called on HTTP error or resp.status of "failure"
	var doRequest = THIS.doRequest = function(options) {
		var onFailFunc = options.onFailure || $ae.defaultRequestError;
		var onSuccess = options.onSuccess || $ae.emptyFunc;
		var ctx = options.ctx || callbackCtx;
		var promise =
			$.ajax({
				type: options.method,
				url: options.url,
				data: options.data || null,
				success: $ae.emptyFunc,
				dataType: options.datatype || "json",
				contentType: options.contentType || "application/json;charset=ISO-8859-1"
			});
		promise.done(
			function (resp) {
				onSuccess.call(ctx, resp);
			}
		);
		if(onFailFunc) {
			promise.fail(function(resp) {
				onFailFunc.call(ctx, resp);
			});
		}
		return promise;
	};
	// -----------------------------------------------------------------
	THIS.formSubmitter = function (formSel, url, options) {
		if(!options) options = {};
		var form = $ae.getSelector(formSel);
		var method = options.method ? options.method : "POST";
		var okfunc = options.onSuccess ? options.onSuccess : function(resp){alert("Form Success: " + formSel)};
		var failfunc = options.onFailFunc ? options.onFailFunc : $ae.showFailure;
		var datafunc = options.datafunc;

		form.submit(
			function (event) {
				event.preventDefault();
				event.stopPropagation();
				var data = $ae.getFormData(form);
				if(datafunc) {
					data = datafunc(data);
				}
				var jdata =  ((typeof data) == "string") ? data : JSON.stringify(data);

				var reqOpts = {
					method: method,
					url: url,
					data: jdata,
					onFailFunc: failfunc,
					onSuccess: okfunc
				};
				if(ctx) reqOpts.ctx = ctx;
				doRequest(reqOpts);
				return false;
			}
		);
	};



	// Create our "Global" named object
	if(! window.$ae ) {
		window.$ae = {}
	}
	window.$ae = _.extend({}, window.$ae, THIS);


}).call(this);