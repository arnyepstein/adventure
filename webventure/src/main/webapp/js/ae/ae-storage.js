(function() {

	var Local =
	{
		get: function(name)
		{
			if(typeof(Storage) !== "undefined") {
				return sessionStorage.getItem(name);
			} else {
				return null;
			}
		},
		getJson: function(name)
		{
			if(typeof(Storage) !== "undefined") {
				var value = sessionStorage.getItem(name);
				return value ? JSON.parse(value) : null;
			} else {
				return null;
			}
		},
		set: function(name, value)
		{
			if(typeof(Storage) !== "undefined") {
				sessionStorage.setItem(name, value);
			}
		},
		setJson: function(name, value)
		{
			if(typeof(Storage) !== "undefined") {
				sessionStorage.setItem(name, JSON.stringify(value));
			}
		},
		remove: function(name)
		{
			if(typeof(Storage) !== "undefined") {
				sessionStorage.removeItem(name);
			}
		}

	};

	$ae.Local = Local;


}).call(this);