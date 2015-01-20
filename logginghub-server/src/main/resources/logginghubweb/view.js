var global_session;

function setupView(div) {
	
	console.log('Div passed is was', div);
	
	var builder = Builder(div);
	
	var view = $.cookie('view');
	
	console.log("View cookie was ", view);
	
	if(view === undefined) {
		showLogon(builder);
	}else{
	
		// TODO : implement a view dispatcher
		if(view === "logon") {
			showLogon(builder);
		}else if(view === "maintenance") {
			showMaintenance(builder);
		}else if(view === "main") {
			showMainScreen(builder);
		}else{
			showLogon(builder);
		}
	}
	
	
	
}

function showLogon(builder) {
	
	builder.target().empty();
	var formDiv = builder.subdiv();
	
	formDiv.appendSpan("Username");
	var usernameInput = formDiv.appendInput("username", "admin");
	
	formDiv.appendSpan("Password");
	var passwordInput = formDiv.appendPassword("password", "admin");
	
	var button = formDiv.appendButton("Logon");
	
	var errorDiv = builder.appendDiv("error message");
	
	button.target().on( "click", function() {
		logon(usernameInput.target().val(), passwordInput.target().val(), function(data) {
        	var obj = jQuery.parseJSON(data);
//        	alert(JSON.stringify(obj, null, 4));
        	if(obj.success) {
        		global_session = obj;
        		showMainScreen(builder);
        	}else{
        		errorDiv.target().text("foo : " + obj.reason);		
        	}
            			
		});		
	});
	
}

function showMainScreen(builder) {
	$.cookie('view', 'main');
	builder.target().empty();
	
	if(global_session === undefined){
		builder.appendDiv("SessionID = <NO SESSION>");
	}else{
		builder.appendDiv("SessionID = " + global_session.sessionID);
		builder.appendDiv("User = " + global_session.userName);
		builder.appendDiv("Default account: ");
		
		builder.appendDiv().appendLink(global_session.defaultAccountName, "#", function() {
			showAccountView(builder,  global_session.defaultAccountName);	
		});
	}
	
	
	builder.appendH1("Select an option:");
	
	builder.appendDiv().appendLink("maintenance", "#maintenance", function() {
		showMaintenance(builder);	
	});
	
	builder.appendDiv().appendLink("view prices", "#prices", function() {
		showPriceView(builder);	
	});
	
	builder.appendDiv().appendLink("logout", "#logout", function() {
		global_session = null;
		showLogon(builder);
	});
	
}

function showPriceViewForInstrument(builder, instrument) {
	$.cookie('view', 'prices');
	builder.target().empty();
	
	builder.appendDiv().appendLink("back", "#", function() {
		showPriceView(builder);	
	});
	
	builder.appendDiv().appendLink("RFS 1 million", "#", function() {
		showRFSView(builder, instrument, 1000000);	
	});
	
	builder.appendDiv().appendLink("RFS 2 million", "#", function() {
		showRFSView(builder, instrument, 2000000);	
	});
	
	builder.appendDiv().appendLink("RFS 3 million", "#", function() {
		showRFSView(builder, instrument, 3000000);	
	});
	
	builder.appendDiv().appendLink("RFS 5 million", "#", function() {
		showRFSView(builder, instrument, 5000000);	
	});
	
	
	builder.appendDiv("Latest price for : " + instrument);
	
	console.log("Opening websockets connection to localhost...");
	
	var connection = new WebSocket("ws://127.0.0.1:8080");

	var bidPriceArray = [];	
	var askPriceArray = [];
	
	var bidQuantityArray = [];
	var askQuantityArray = [];
	
	var bidLPArray = [];
	var askLPArray = [];
	
	var levels = 20;
	var div = builder.appendDiv("Price data");
	
	var table = div.appendTable();
	
	for(var i = 0; i < levels; i++) {
		var row = table.appendRow();		
		
		bidLPArray[i] = row.appendCell();
		bidPriceArray[i] = row.appendCell();
		bidQuantityArray[i] = row.appendCell();
		
		askLPArray[i] = row.appendCell();
		askPriceArray[i] = row.appendCell();
		askQuantityArray[i] = row.appendCell();
	}				
	
	connection.onopen = function(){
	   console.log('Connection open');
	   
	   var subscription = {
		   action: "subscribe",
		   channel: "prices/" + instrument
	   };
	   
	   connection.send(JSON.stringify(subscription));
	}
	
	connection.onclose = function(){
	   console.log('Connection closed');
	}
	
	connection.onerror = function(error){
	   console.log('Error detected: ', error);
	}
	
	connection.onmessage = function(e){
	   var object = jQuery.parseJSON(e.data);
//	   console.log("Updating price : ", object);

	   var bids = object.bids;
	   for(var i = 0; i < bids.length; i++) {
		   bidLPArray[i].target().text(bids[i].lp);
		   bidPriceArray[i].target().text(bids[i].price);
		   bidQuantityArray[i].target().text(bids[i].quantity);
	   }
	   
	   var asks = object.asks;
	   for(var i = 0; i < asks.length; i++) {
		   askLPArray[i].target().text(asks[i].lp);
		   askPriceArray[i].target().text(asks[i].price);
		   askQuantityArray[i].target().text(asks[i].quantity);
	   }
	}
	
}	

function showRFSView(builder, instrument, rfsLevel) {
	$.cookie('view', 'prices');
	builder.target().empty();
	
	builder.appendDiv().appendLink("back", "#", function() {
		showPriceView(builder);	
	});
	
	builder.appendDiv("RFS price for : " + instrument + " for quantity " + rfsLevel);
	
	console.log("Opening websockets connection to localhost...");
	
	var connection = new WebSocket("ws://127.0.0.1:8080");

	var bidSpan = builder.appendSpan("bid");
	var offerSpan = builder.appendSpan("offer");

	var buttons = builder.appendDiv();
	
	var responseDiv = builder.appendDiv();
	var handler = function(response) {
		responseDiv.target().text(JSON.stringify(response));
	}
	
	buttons.appendButton("Buy", function() {
		placeOrder(instrument, rfsLevel, "buy", handler);
	});
	
	buttons.appendButton("Sell", function() { 
		placeOrder(instrument, rfsLevel, "sell", handler);
	});
	
	connection.onopen = function(){
	   console.log('Connection open');
	   
	   var subscription = {
		   action: "subscribe",
		   channel: "rfs/" + instrument + "/" + rfsLevel
	   };
	   
	   connection.send(JSON.stringify(subscription));
	}
	
	connection.onclose = function(){
	   console.log('Connection closed');
	}
	
	connection.onerror = function(error){
	   console.log('Error detected: ', error);
	}
	
	connection.onmessage = function(e){
	   var object = jQuery.parseJSON(e.data);
	   //console.log("Updating price : ", object);

	   bidSpan.target().text("bid: " + object.bid);
	   offerSpan.target().text("offer: " + object.offer);
	}
	
}	

function creditAccount(accountName, quantity, handler) {
	$.ajax({
        type: "POST",
        url: "creditAccount",
        data: {
        	accountName: accountName,
        	quantity: quantity,
        },
        success: function(data) { 
        	console.log(data);
        	var response = jQuery.parseJSON(data);
        	handler(response);
        }
    });
}

function placeOrder(instrument, quantity, side, handler) {
	$.ajax({
        type: "POST",
        url: "placeOrder",
        data: {
        	instrument: instrument,
        	quantity: quantity,
        	side: side
        },
        success: function(data) { 
        	console.log(data);
        	var response = jQuery.parseJSON(data);
        	handler(response);
        }
    });
}

function showPriceView(builder) {
	$.cookie('view', 'prices');
	builder.target().empty();
	
	builder.appendDiv("Please select an instrument:");
	
	$.ajax({
        type: "POST",
        url: "listInstruments",
        data: {
        },
        success: function(data) { 
        	var array = jQuery.parseJSON(data).instruments;
        	
        	for(var i = 0; i < array.length; i++) {
        		var instrument = array[i];        		
        		builder.appendDiv().appendLink(instrument.name, "#", function() {
        			showPriceViewForInstrument(builder, instrument.name);	
        		});  		
        	}
        }
    });		
}

function showAccountView(builder, accountName) {
	$.cookie('view', 'prices');
	builder.target().empty();
	
	builder.appendDiv().appendLink("back", "#", function() {
		showMainScreen(builder);	
	});	
	
	builder.appendDiv("Account : " + accountName);
	var result = builder.appendDiv();
	
	$.ajax({
        type: "POST",
        url: "viewAccount",
        data: {
        	accountName: accountName
        },
        success: function(data) {
        	result.target().text(data);
        }
    });
		
}

function showMaintenance(builder) {
	$.cookie('view', 'maintenance');
	builder.target().empty();
	
	builder.appendLink("back", "#", function() {
		showMainScreen(builder);	
	});
	
	var div = builder.appendDiv();
	
	addMaintenanceBar(builder);	
}

function showAccountMainenance(builder) {
	$.cookie('view', 'maintenance');
	builder.target().empty();
	
	builder.appendLink("back", "#", function() {
		showMainScreen(builder);	
	});
	
	addMaintenanceBar(builder);	
	addAccountMaintenance(builder);
}

function showUserMainenance(builder) {
	$.cookie('view', 'maintenance');
	builder.target().empty();
	
	builder.appendLink("back", "#", function() {
		showMainScreen(builder);	
	});
	
	addMaintenanceBar(builder);	
	addUserMaintenance(builder);
}


function showUserAccountRelationshipMainenance(builder) {
	$.cookie('view', 'maintenance');
	builder.target().empty();
	
	builder.appendLink("back", "#", function() {
		showMainScreen(builder);	
	});
	
	addMaintenanceBar(builder);	
	addUserAccountRelationship(builder);
}


function showInstrumentMainenance(builder) {
	$.cookie('view', 'maintenance');
	builder.target().empty();
	
	builder.appendLink("back", "#", function() {
		showMainScreen(builder);	
	});
	
	addMaintenanceBar(builder);	
	addInstrumentMaintenance(builder);
}

function addMaintenanceBar(builder) {
	var div = builder.appendDiv();
	var row = div.appendTable().appendRow();
	row.appendCell().appendLink("Users", "#", function() {
		showUserMainenance(builder);
	});
	
	row.appendCell().appendLink("Accounts", "#", function() {
		showAccountMainenance(builder);
	});
	
	row.appendCell().appendLink("UserAccountRelationships", "#", function() {
		showUserAccountRelationshipMainenance(builder);
	});
	
	row.appendCell().appendLink("Instruments", "#", function() {
		showInstrumentMainenance(builder);
	});
}

function addUserAccountRelationship(builder) {
	// Create the quick-add user form
	var form = builder.appendDiv();
	form.appendSpan("Username");
	var userNameInput = form.appendInput("userName", "userName");
	var accountNameInput = form.appendInput("accountName", "accountName");
	var relationshipInput = form.appendInput("relationship", "relationship");
	var submitButton = form.appendSubmit("Add relationship");
	var errorSpan = form.appendSpan("");
	
	// Create the users table
	var div = builder.appendDiv("Relationships:");
	
	var table = div.appendTable();
	var header = table.appendTableHeader();
	var headerRow = header.appendRow();
	headerRow.appendCell("Username");
	headerRow.appendCell("Account");
	headerRow.appendCell("Relationship");
	headerRow.appendCell("Created");
	
	var body = table.appendTableBody();

	// Bind the add users button
	submitButton.target().on( "click", function() {
		console.log("Submitting new user account relationship", userNameInput.target().val(), accountNameInput.target().val(),  relationshipInput.target().val());
		$.ajax({
	        type: "POST",
	        url: "createUserAccountRelationship",
	        data: {
	        	userName: userNameInput.target().val(),
	        	accountName: accountNameInput.target().val(),  
	        	relationship: relationshipInput.target().val()
	        },	
	        success: function(data) { 
	        	console.log(data);
	        	
	        	var response = jQuery.parseJSON(data);

	        	if(response.success == true) {
	        		var row = body.appendRow();
	        		row.appendCell(response.userName);
	        		row.appendCell(response.accountName);
	        		row.appendCell(response.relationship);
	        		row.appendCell(response.created);
	        		
	        		form.target().effect("highlight", { color:"#00ff00" }, 500);
	        		errorSpan.target().text("");
	        		  
	        	}else{
	        		errorSpan.target().text(response.reason);
	        		
	        		form.target().effect("highlight", { color:"#ff0000" }, 500);
	        	}
	        }
	    });
	});
	
	// Request the user lists asynchronously	 	
	$.ajax({
        type: "POST",
        url: "listUserAccountRelationships",
        data: {
        	// This is done via a session cookie now
        	//sessionID: global_session.sessionID
        },
        success: function(data) { 
        	
        	console.log(data);
        	
        	var relationshipsArray = jQuery.parseJSON(data).relationships;
        	
        	for(var i = 0; i < relationshipsArray.length; i++) {
        		var relationship = relationshipsArray[i];
        		var row = body.appendRow();
        		row.appendCell(relationship.userName);
        		row.appendCell(relationship.accountName);
        		row.appendCell(relationship.relationship);
        		row.appendCell(relationship.created);
        	}
        }
    });
}

function addUserMaintenance(builder) {
	// Create the quick-add user form
	var form = builder.appendDiv();
	form.appendSpan("New username");
	var usernameInput = form.appendInput("username", "username");
	var submitButton = form.appendSubmit("Add user");
	var errorSpan = form.appendSpan("");
	
	// Create the users table
	var div = builder.appendDiv("Users:");
	
	var table = div.appendTable();
	var header = table.appendTableHeader();
	var headerRow = header.appendRow();
	headerRow.appendCell("Username");
	headerRow.appendCell("Created");
	
	var body = table.appendTableBody();

	// Bind the add users button
	submitButton.target().on( "click", function() {
		console.log("Submitting new username", usernameInput.target().val());
		$.ajax({
	        type: "POST",
	        url: "createUser",
	        data: {
	        	username: usernameInput.target().val()
	        },
	        success: function(data) { 
	        	console.log(data);
	        	
	        	var response = jQuery.parseJSON(data);

	        	if(response.success == true) {
	        		var row = body.appendRow();
	        		row.appendCell(response.username);
	        		row.appendCell(response.created);
	        		
	        		form.target().effect("highlight", { color:"#00ff00" }, 500);
	        		errorSpan.target().text("");
	        		  
	        	}else{
	        		errorSpan.target().text(response.reason);
	        		form.target().effect("highlight", { color:"#ff0000" }, 500);
	        	}
	        }
	    });
	});
	
	// Request the user lists asynchronously	 	
	$.ajax({
        type: "POST",
        url: "listUsers",
        data: {
        	// This is done via a session cookie now
        	//sessionID: global_session.sessionID
        },
        success: function(data) { 
        	
        	console.log(data);
        	
        	var usersArray = jQuery.parseJSON(data).users;
        	
        	for(var i = 0; i < usersArray.length; i++) {
        		var user = usersArray[i];
        		var row = body.appendRow();
        		row.appendCell(user.username);
        		row.appendCell(user.created);
        	}
        }
    });
}

function addInstrumentMaintenance(builder) {
		
	// Create the quick-add user form
	var form = builder.appendDiv();
	form.appendSpan("New instrument");
	var nameInput = form.appendInput("name", "name");
	var submitButton = form.appendSubmit("Add instrument");
	var errorSpan = form.appendSpan("");
	
	// Create the users table
	var div = builder.appendDiv("Instruments:");
	
	var table = div.appendTable();
	var header = table.appendTableHeader();
	var headerRow = header.appendRow();
	headerRow.appendCell("Name");
	headerRow.appendCell("Decimal places");
	headerRow.appendCell("Created");
	
	var body = table.appendTableBody();

	// Bind the add users button
	submitButton.target().on( "click", function() {
		console.log("Submitting new instrument name", nameInput.target().val());
		$.ajax({
	        type: "POST",
	        url: "createInstrument",
	        data: {
	        	name: nameInput.target().val(),
	        	decimalPlaces: 5
	        },
	        success: function(data) { 
	        	console.log(data);
	        	
	        	var response = jQuery.parseJSON(data);

	        	if(response.success == true) {
	        		var row = body.appendRow();
	        		row.appendCell(response.name);
	        		row.appendCell(response.decimalPlaces);
	        		row.appendCell(response.created);
	        		
	        		form.target().effect("highlight", { color:"#00ff00" }, 500);
	        		errorSpan.target().text("");
	        		  
	        	}else{
	        		errorSpan.target().text(response.reason);
	        		
	        		form.target().effect("highlight", { color:"#ff0000" }, 500);
	        	}
	        }
	    });
	});
	
	// Request the user lists asynchronously	 	
	$.ajax({
        type: "POST",
        url: "listInstruments",
        data: {
        	// This is done via a session cookie now
        	//sessionID: global_session.sessionID
        },
        success: function(data) { 
        	
        	console.log(data);
        	
        	var array = jQuery.parseJSON(data).instruments;
        	
        	for(var i = 0; i < array.length; i++) {
        		var instrument = array[i];
        		var row = body.appendRow();
        		row.appendCell(instrument.name);
        		row.appendCell(instrument.decimalPlaces);
        		row.appendCell(instrument.created);
        	}
        }
    });
}

function addAccountMaintenance(builder) {
	// Create the quick-add account form
	var form = builder.appendDiv();
	form.appendSpan("New accountname");
	var accountnameInput = form.appendInput("accountname", "accountname");
	var submitButton = form.appendSubmit("Add account");
	var errorSpan = form.appendSpan("");
	
	// Create the accounts table
	var div = builder.appendDiv("Accounts:");
	
	var table = div.appendTable();
	var header = table.appendTableHeader();
	var headerRow = header.appendRow();
	headerRow.appendCell("Accountname");
	headerRow.appendCell("Created");
	
	var body = table.appendTableBody();

	// Bind the add accounts button
	submitButton.target().on( "click", function() {
		console.log("Submitting new accountname", accountnameInput.target().val());
		$.ajax({
	        type: "POST",
	        url: "createAccount",
	        data: {
	        	accountName: accountnameInput.target().val()
	        },
	        success: function(data) { 
	        	console.log(data);
	        	
	        	var response = jQuery.parseJSON(data);

	        	if(response.success == true) {
	        		var row = body.appendRow();
	        		row.appendCell(response.accountName);
	        		row.appendCell(response.created);
	        		
	        		var buttonCell = row.appendCell();
	        		buttonCell.appendButton("Credit", function() {
	        			creditAccount(response.accountName);
	        		});
	        		
	        		form.target().effect("highlight", { color:"#00ff00" }, 500);
	        		errorSpan.target().text("");
	        		  
	        	}else{
	        		errorSpan.target().text(response.reason);
	        		
	        		form.target().effect("highlight", { color:"#ff0000" }, 500);
	        	}
	        }
	    });
		
	});
	
	
	// Request the account lists asynchronously	 	
	$.ajax({
        type: "POST",
        url: "listAccounts",
        data: {
        	// This is done via a session cookie now
        	//sessionID: global_session.sessionID
        },
        success: function(data) { 
        	
        	console.log(data);
        	
        	var accountsArray = jQuery.parseJSON(data).accounts;
        	
        	for(var i = 0; i < accountsArray.length; i++) {
        		var account = accountsArray[i];
        		var row = body.appendRow();
        		row.appendCell(account.accountName);
        		row.appendCell(account.created);
        		
        		var buttonCell = row.appendCell();
        		buttonCell.appendButton("Credit", function() {
        			creditAccount(response.accountName);
        		});
        	}
        }
    });
	
}

function logon(username, password, handler) {
	$.ajax({
        type: "POST",
        url: "logon",
        data: { 
        	username: username,
        	password: password	
        },
        success: handler
    });
}

function request(builder, orderID) {
	$.get( "getOrder/" + orderID, function( data ) {
		  console.log(data);		  
	
		  var message = jQuery.parseJSON(data);
		  
		  console.log(message);		  
		  
		  var priceView = builder.subSpan();
		  priceView.appendDiv("Time : " + message.report.predata.observablemarketdata[0].time);
		  priceView.appendDiv("ccy : " + message.report.predata.observablemarketdata[0].ccy);
		  
		  var pricesArray = message.report.predata.observablemarketdata[0].depths.observablemarketdepth;
		  console.log("Prices array", pricesArray);
		  var first = pricesArray[0];
		  console.log("First", first);
		  priceView.appendDiv("Best price : " + pricesArray[0].price);
		  
		  for (var i = 0; i < pricesArray.length; i++) {
			  var price = pricesArray[i];
			  priceView.appendDiv("Level " + price.level + " : " + price.price + " : " + price.quantity + "  [min : " + price.minimumOrderQuantity + " max : " + price.maximumOrderQuantity + "]");
		  }
		  
		  priceView.target().addClass("inline");
		  
		  
		  var orderView = builder.subSpan();
		  orderView.appendDiv("Order id : " + message.report.order.orderID);
		  orderView.appendDiv("Status : " + message.report.order.status);
		  orderView.appendDiv("PlacedTime : " + message.report.order.placedTime);
		  orderView.appendDiv("TransactionTime : " + message.report.order.transactionTime);
		  orderView.appendDiv("ccy : " + message.report.order.ccy);		  		  		
		  
		  orderView.appendDiv("orderQuantity : " + message.report.order.orderQuantity);
		  orderView.appendDiv("leavesQuantity : " + message.report.order.leavesQuantity);
		  
		  orderView.appendDiv("price : " + message.report.order.price);
		  orderView.appendDiv("lastFilledPrice : " + message.report.order.lastFilledPrice);
		  orderView.appendDiv("averageFilledPrice : " + message.report.order.averageFilledPrice);
		  
		  orderView.target().addClass("inline");
		  
	});
	
}



function showOrderRequest(builder) {

	var formDiv = builder.subdiv();
	formDiv.appendSpan("Order ID : ");
	var input = formDiv.appendInput("orderID", "14005840057084161");
	var button = formDiv.appendButton("Request");
	
	var content = builder.subdiv();
	
	console.log("Input val is", input.val());
	
	button.target().on( "click", function() {
		content.target().empty();
		request(content, input.val());
	});
}
