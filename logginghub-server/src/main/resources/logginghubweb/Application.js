/**
 * The top level class for the MarketStreamer web app; it pulls all of the other classes together.
 */

function Application() {
	
	
	this.initialise = function(div) { 
		
		var api = new LoggingHubAPI();
		
		var builder = Builder(div);
		
		var screenController = new ScreenController(builder);
		console.log("Screen controller", screenController);

		screenController.addScreen("main", new MainScreen(api));		
		screenController.addScreen("logon", new LogonScreen(api));
		screenController.addScreen("patterns", new PatternsScreen(api));		
		screenController.addScreen("patternStream", new PatternStreamScreen(api));
		
		screenController.addScreen("aggregations", new AggregationsScreen(api));
		screenController.addScreen("aggregationStream", new AggregationStreamScreen(api));
		screenController.addScreen("aggregationChart", new AggregationChartScreen(api));
		
			
		//screenController.setNoSessionScreen("logon");
		screenController.setNoSessionScreen("main");
		screenController.setDefaultScreen("main");
		
		screenController.showScreen("logon");
		
	};
	
}