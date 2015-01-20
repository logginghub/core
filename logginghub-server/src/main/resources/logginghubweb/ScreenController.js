/**
 * 
 */

function ScreenController(builder) {	
	this.builder = builder;
	this.screens = {};	
	this.currentScreen = null;
}

ScreenController.prototype.addScreen = function(name, screen) {	
	this.screens[name] = screen;	
};


ScreenController.prototype.renderHeader = function(builder) {
	
};

ScreenController.prototype.renderFooter = function(builder) {
	
};

ScreenController.prototype.showScreen = function(name, parameters) {
	this.builder.target().empty();
	
	var actualScreen = name;
	if(this.sessionToken === undefined) {
		console.log("No session - redirecting to '" + this.noSessionScreenName);
		actualScreen = this.noSessionScreenName;
	}
	
	if(this.currentScreen) {
		if(this.currentScreen.cleanup) {
			this.currentScreen.cleanup();
		}
	}
	
	var screen = this.screens[actualScreen];
	
	if(screen === undefined) {
		var newParameters = { error:  "No screen defined for '" + actualScreen + "'" };		
		this.showDefaultScreen(newParameters);
		
	}else{
		console.log("Showing screen '" + actualScreen + "'", screen);
		this.currentScreen = screen;
		
		this.renderHeader(this.builder);
		
		var params = parameters || {};
		screen.render(this, this.builder, params);
		
		this.renderFooter(this.builder);
	}	
};

ScreenController.prototype.setSessionToken = function(sessionToken) {
	this.sessionToken = sessionToken;
};

ScreenController.prototype.getSessionToken = function() {
	return this.sessionToken;
};

ScreenController.prototype.showDefaultScreen = function(parameters) {
	this.showScreen(this.defaultScreen, parameters);
};	

ScreenController.prototype.setDefaultScreen = function(defaultScreenName) {
	this.defaultScreen = defaultScreenName;
};

ScreenController.prototype.setNoSessionScreen = function(noSessionScreenName) {
	this.noSessionScreenName = noSessionScreenName;
};