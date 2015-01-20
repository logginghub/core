
var LEVEL_ALL = 0;
var LEVEL_FINEST = 300;
var LEVEL_FINER = 400;
var LEVEL_FINE = 500;
var LEVEL_TRACE = 300;
var LEVEL_DEBUG = 500;
var LEVEL_CONFIG = 700;
var LEVEL_INFO = 800;
var LEVEL_WARNING = 900;
var LEVEL_SEVERE = 1000;

function LoggingHubAPI() {

	this.websocket = undefined;
	this.sessionToken = undefined;

	this.channelSubscriptions = {};
	this.requestHandlers = {};

	this.nextRequestID = 0;

};

LoggingHubAPI.prototype.logon = function(username, password, handler) {

	var _this = this;

	$.ajax({
		type : "POST",
		url : "logon",
		data : {
			userName : username,
			password : password
		},
		success : function(result) {
			// Intercept the session token so we can use it for websockets
			// requests too
			var sessionToken = jQuery.parseJSON(result);
			if (sessionToken.success) {
				_this.sessionToken = sessionToken;
			}

			handler(result);
		}
	});

};

LoggingHubAPI.prototype.getAccount = function(accountName, handler) {
	$.ajax({
		type : "POST",
		url : "viewAccount",
		data : {
			accountName : accountName
		},
		success : function(data) {
			handler(data);
		}
	});
};

LoggingHubAPI.prototype.getPatterns = function(handler) {
	$.ajax({
		type : "POST",
		url : "getPatterns",
		data : {			
		},
		success : function(data) {
			var object= jQuery.parseJSON(data);
			handler(object);
		}
	});
};

LoggingHubAPI.prototype.getAggregations = function(handler) {
	$.ajax({
		type : "POST",
		url : "getAggregations",
		data : {			
		},
		success : function(data) {
			var object= jQuery.parseJSON(data);
			handler(object);
		}
	});
};


LoggingHubAPI.prototype.createPattern = function(name, pattern, handler) {
	$.ajax({
		type : "POST",
		url : "createPattern",
		data : {			
			name: name,
			pattern: pattern
		},
		success : function(data) {
			handler(jQuery.parseJSON(data));
		}
	});
};

LoggingHubAPI.prototype.createAggregation = function(pattern, label, interval, type, handler) {
	$.ajax({
		type : "POST",
		url : "createAggregation",
		data : {			
			pattern: pattern,
			label: label,
			interval: interval,
			type: type
		},
		success : function(data) {
			handler(jQuery.parseJSON(data));
		}
	});
};

LoggingHubAPI.prototype.send = function(data, handler) {

	console.log("Sending message", data);

	var messageList = [];

	var _this = this;

	if (this.websocket === undefined) {

		messageList.push(data);

		console.log("Creating websockets connection");

		// TODO : how do we work out where to connect to once deployed?
		this.websocket = new WebSocket("ws://" + document.location.host + "/xx");

		this.websocket.onopen = function() {
			console.log('Connection open - sending queued messages');
			for (var i = 0; i < messageList.length; i++) {
				var message = messageList[i];
				console.log("Sending queued message", data);
				this.send(message);
			}

			console.log("Message backlog cleared");
			meessageList = [];

		};

		this.websocket.onclose = function() {
			console.log('Connection closed');
		};

		this.websocket.onerror = function(error) {
			console.log('Error detected: ', error);
		};

		this.websocket.onmessage = function(message) {
//			console.log('New message  : ', message);

			var response = jQuery.parseJSON(message.data);

//			console.log('Message data is', response);
			
			// See if it was for a registered request?
			var requestID = response.requestID;
			if (!(requestID === undefined)) {
				var requestHandler = _this.requestHandlers[requestID];

				console.log("Found request handler for requestID '" + requestID + "'", requestHandler);

				if (requestHandler) {
					_this.requestHandlers[requestID] = undefined;

					if(typeof(requestHandler) == "function") {
						requestHandler(response);
					}else{
						// Maybe its an object with handlers for each different type?
						if (response.state === "Successful") {
							requestHandler.onSuccessful(response);
						} else if (response.state === "Unsuccessful") {
							requestHandler.onUnsuccessful(response);
						} else if (response.state === "Failed") {
							requestHandler.onFailed(response);
						}
					}	

				}
			} else {
				// Check for a channel subscription
				var channel = response.channel;
				if (channel) {
//					console.log("Message is an update on channel", channel);
					var handlerList = _this.channelSubscriptions[channel];
					
//					console.log("Handler list for channel is ", handlerList);
					if (handlerList) {
						
						//var value = jQuery.parseJSON(response.value);
						var value = response.value;
						
//						console.log("Update object is ", value);
						
						
						for (var i = 0; i < handlerList.length; i++) {
							var handler = handlerList[i];							
//							console.log("Invoker handler", handler);
							handler(value);
						}
					}
				}
			}

		};
	}

	else {
		if (this.websocket.readyState == this.websocket.OPEN) {
			console.log("Sending message", data);
			this.websocket.send(data);
		} else {
			console.log("Websocket isn't open yet, queing message", data);
			messageList.push(data);
		}
	}

};

LoggingHubAPI.prototype.getNextRequestID = function() {
	var id = this.nextRequestID;
	this.nextRequestID++;
	return id;
};

LoggingHubAPI.prototype.unsubscribe = function(channel) {
	// TODO : this will unsubscribe _everything_ from this channel, need something that will unsubscribe an individual handler 
	var requestID = this.getNextRequestID();

	var message = {
		requestID : requestID,
		action : "unsubscribe",
		channel : channel
	};
	
	this.send(JSON.stringify(message));
	
	this.channelSubscriptions[channel] = undefined;
}

LoggingHubAPI.prototype.subscribe = function(channel, requestHandler, subscriptionHandler) {

	var requestID = this.getNextRequestID();

	var message = {
		requestID : requestID,
		action : "subscribe",
		channel : channel
	};

	// Register the request handler for notifications based on the request
	this.requestHandlers[requestID] = requestHandler;

	// TODO : move this into a successful response handler
	// Register the subscription to the channel
	var handlerList = this.channelSubscriptions[channel];
	if (handlerList === undefined) {
		handlerList = [];
		this.channelSubscriptions[channel] = handlerList;
	}

	handlerList.push(subscriptionHandler);

	// Send the message
	this.send(JSON.stringify(message));
};

LoggingHubAPI.prototype.subscribeAndNotifyCurrent = function(type, key, subscriptionHandler) {

	var requestID = this.getNextRequestID();

	var message = {
		requestID : requestID,
		action : "subscribeAndNotifyCurrent",
		type : type,
		key : key,
		sessionID : this.sessionToken.sessionID,
	};

	var _this = this;
	
	var requestHandler = {
		onSuccessful : function(response) {
			console.log("onSuccessful - subscribing locally", response);

			var channel = "updates/" + type + "/" + key;

			// Register the subscription to the channel
			var handlerList = _this.channelSubscriptions[channel];
			if (handlerList === undefined) {
				handlerList = [];
				_this.channelSubscriptions[channel] = handlerList;
			}

			handlerList.push(subscriptionHandler);

			// Send the 'current' object to the handler
			subscriptionHandler(response.value);
			
		},
		onUnsuccessful : function(response) {
			console.log("onUnsuccessful - reason", reponse.reason);
		},
		onFailure : function(response) {
			console.log("onFailure - reason", reponse.reason);
		},
		onTimeout : function() {
			console.log("onTimeout");
		}
	};

	// Register the request handler for notifications based on the request
	this.requestHandlers[requestID] = requestHandler;

	// Send the message
	this.send(JSON.stringify(message));

};
