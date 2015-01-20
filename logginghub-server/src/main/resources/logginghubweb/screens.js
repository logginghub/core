/**
 * 
 */

function TemplateScreen(api) {
	this.api = api;
	this.render = function(screenController, builder, parameters) {

	};
};

function PatternStreamScreen(api) {
	this.api = api;
	this.channel;

	this.cleanup = function() {
		this.api.unsubscribe(this.channel);
	};

	this.render = function(screenController, builder, parameters) {
		var toolbarDiv = builder.appendDiv("toolbar");

		$().w2destroy('toolbar');

		toolbarDiv.target().w2toolbar({
			name : 'toolbar',
			onClick : function(target, info) {
				console.log(target, info);
				if (info.item.id == 'home') {
					screenController.showScreen("main");
				} else if (info.item.id == 'patterns') {
					screenController.showScreen("patterns");
				}
			},
			items : [ 
			          {
				type : 'button',
				id : 'home',
				caption : 'Home'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'patterns',
				caption : 'View Patterns'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'patternStream',
				caption : 'Pattern Stream : ' + parameters.pattern.name
			} ]
		});

		var table = builder.appendTable();
		var tableHeader = builder.appendTableHeader();
		var tableHeaderRow = tableHeader.appendRow();
		// tableHeaderRow.appendHeaderCell("ID");
		// tableHeaderRow.appendHeaderCell("Name");
		// tableHeaderRow.appendHeaderCell("Pattern");

		var tableBody = builder.appendTableBody();

		this.channel = "updates/patternised/" + parameters.pattern.patternID

		this.api.subscribe(this.channel, function(subscriptionResponse) {
			console.log("Subscription response", subscriptionResponse);
		}, function(update) {
			console.log("Subscription update", update);

			var row = tableBody.appendRow();

			for ( var property in update.data) {
				if (update.data.hasOwnProperty(property)) {
					row.appendCell(update.data[property]);
				}
			}
		});

	};
};

function AggregationStreamScreen(api) {
	this.api = api;
	this.channel;

	this.cleanup = function() {
		this.api.unsubscribe(this.channel);
	};

	this.render = function(screenController, builder, parameters) {
		var toolbarDiv = builder.appendDiv("toolbar");

		$().w2destroy('toolbar');

		toolbarDiv.target().w2toolbar({
			name : 'toolbar',
			onClick : function(target, info) {
				console.log(target, info);
				if (info.item.id == 'home') {
					screenController.showScreen("main");
				} else if (info.item.id == 'aggregations') {
					screenController.showScreen("aggregations");
				}
			},
			items : [ 
			          {
				type : 'button',
				id : 'home',
				caption : 'Home'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'aggregations',
				caption : 'View Aggregations'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'patternStream',
				caption : 'Aggregation Stream : ' + parameters.aggregation.pattern+ "::" + parameters.aggregation.interval  + "::" + parameters.aggregation.type
			} ]
		});

		var table = builder.appendTable();
		var tableHeader = builder.appendTableHeader();
		var tableHeaderRow = tableHeader.appendRow();
		tableHeaderRow.appendHeaderCell("Time");
		tableHeaderRow.appendHeaderCell("Value");

		var tableBody = builder.appendTableBody();

		this.channel = "updates/aggregated/" + parameters.aggregation.pattern+ "/" + parameters.aggregation.label + "/" + parameters.aggregation.type + "/" + parameters.aggregation.interval

		this.api.subscribe(this.channel, function(subscriptionResponse) {
			console.log("Subscription response", subscriptionResponse);
		}, function(update) {
			console.log("Subscription update", update);

			var row = tableBody.appendRow();

			row.appendCell(new Date(update.time));
			row.appendCell(update.value);			
			}
		);

	};
};

function AggregationChartScreen(api) {
	this.api = api;
	this.channel;

	this.cleanup = function() {
		this.api.unsubscribe(this.channel);
	};

	this.render = function(screenController, builder, parameters) {
		var toolbarDiv = builder.appendDiv("toolbar");

		$().w2destroy('toolbar');

		toolbarDiv.target().w2toolbar({
			name : 'toolbar',
			onClick : function(target, info) {
				console.log(target, info);
				if (info.item.id == 'home') {
					screenController.showScreen("main");
				} else if (info.item.id == 'aggregations') {
					screenController.showScreen("aggregations");
				}
			},
			items : [ 
			          {
				type : 'button',
				id : 'home',
				caption : 'Home'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'aggregations',
				caption : 'View Aggregations'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'patternStream',
				caption : 'Aggregation Stream : ' + parameters.aggregation.pattern+ "::" + parameters.aggregation.interval  + "::" + parameters.aggregation.type
			} ]
		});

		var lineChartData = [
		                     // First series
		                     {
		                       label: "Series 1",
		                       values: [ {time: 1370044800, y: 0} ]
		                     },

		                    
		                    
		                   ];
		
		var div = builder.appendDiv();
		div.target().attr("class", "epoch epoch-theme-dark");
		div.target().css("width", "90%")
		div.target().css("height", "90%")
		var chart = div.target().epoch({
			  type: 'time.line',
			  data: lineChartData,			  
			  axes: ['top', 'right', 'bottom', 'left'],
			  
			  
		});
		

		this.channel = "updates/aggregated/" + parameters.aggregation.pattern+ "/" + parameters.aggregation.label + "/" + parameters.aggregation.type + "/" + parameters.aggregation.interval

		this.api.subscribe(this.channel, function(subscriptionResponse) {
			console.log("Subscription response", subscriptionResponse);
		}, function(update) {
			console.log("Subscription update", update);
			
			var dataPoint = [];
			dataPoint.push({time: update.time, y: update.value});
			chart.push(dataPoint);

		}
		);

	};
};

function PatternsScreen(api) {
	this.api = api;
	this.render = function(screenController, builder, parameters) {
		var _this = this;
		var toolbarDiv = builder.appendDiv("toolbar");

		$().w2destroy('patterns-toolbar');

		toolbarDiv.target().w2toolbar({
			name : 'patterns-toolbar',
			onClick : function(target, info) {
				console.log(target, info);
				if (info.item.id == 'home') {
					screenController.showScreen("main");
				}
			},
			items : [
			{
				type : 'button',
				id : 'home',
				caption : 'Home'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'addPattern',
				caption : 'View Patterns'
			} ]
		});

		var table = builder.appendTable();
		var tableHeader = builder.appendTableHeader();
		var tableHeaderRow = tableHeader.appendRow();
		tableHeaderRow.appendHeaderCell("ID");
		tableHeaderRow.appendHeaderCell("Name");
		tableHeaderRow.appendHeaderCell("Pattern");

		var tableBody = builder.appendTableBody();

		var row = tableBody.appendRow();
		row.appendCell("");
		var nameInput = row.appendCell().appendInput("name");
		var patternInput = row.appendCell().appendInput("pattern");

		var addButtonCell = row.appendCell();
		var statusCell = row.appendCell("status");

		var rowMapper = function(pattern) {
			var row = tableBody.appendRow();
			row.appendCell(pattern.patternID);
			row.appendCell(pattern.name);
			row.appendCell(pattern.pattern);

			row.appendCell().appendButton("View stream", function() {
				console.log("View for pattern", pattern);

				screenController.showScreen("patternStream", {
					pattern : pattern
				});
			});

		};

		var addButton = addButtonCell.appendButton("Add", function() {
			statusCell.target().text("");

			_this.api.createPattern(nameInput.target().val(), patternInput.target().val(), function(result) {
				console.log(result);

				if (result.state == "Successful") {
					var pattern = result.value;

					rowMapper(pattern);

					nameInput.target().effect("highlight", {
						color : "#00ff00"
					}, 500);
					patternInput.target().effect("highlight", {
						color : "#00ff00"
					}, 500);

				} else {

					statusCell.target().text(result.externalReason);

					nameInput.target().effect("highlight", {
						color : "#ff0000"
					}, 500);
					patternInput.target().effect("highlight", {
						color : "#ff0000"
					}, 500);
				}
			});
		});

		this.api.getPatterns(function(result) {
			var patterns = result.value || [];
			for (var i = 0; i < patterns.length; i++) {
				var pattern = patterns[i];
				rowMapper(pattern);
			}
		});

	};
};

function AggregationsScreen(api) {
	this.api = api;
	this.render = function(screenController, builder, parameters) {
		var _this = this;
		var toolbarDiv = builder.appendDiv("toolbar");

		$().w2destroy('toolbar');

		toolbarDiv.target().w2toolbar({
			name : 'toolbar',
			onClick : function(target, info) {
				console.log(target, info);
				if (info.item.id == 'home') {
					screenController.showScreen("main");
				}
			},
			items : [
			{
				type : 'button',
				id : 'home',
				caption : 'Home'
			}, {
				type : 'break',
				id : 'break2'
			}, {
				type : 'button',
				id : 'addPattern',
				caption : 'View Aggregations'
			} ]
		});

		var table = builder.appendTable();
		var tableHeader = builder.appendTableHeader();
		var tableHeaderRow = tableHeader.appendRow();
		tableHeaderRow.appendHeaderCell("ID");
		tableHeaderRow.appendHeaderCell("Pattern");
		tableHeaderRow.appendHeaderCell("Label");
		tableHeaderRow.appendHeaderCell("Interval");
		tableHeaderRow.appendHeaderCell("Type");

		var tableBody = builder.appendTableBody();

		var row = tableBody.appendRow();
		row.appendCell("");
		var patternInput = row.appendCell().appendInput("pattern");
		var labelInput = row.appendCell().appendInput("label");
		var intervalInput = row.appendCell().appendInput("interval");
		var typeInput = row.appendCell().appendInput("type");

		var addButtonCell = row.appendCell();
		var statusCell = row.appendCell("status");

		var rowMapper = function(aggregation) {
			var row = tableBody.appendRow();
			row.appendCell(aggregation.aggregationID);
			row.appendCell(aggregation.pattern);
			row.appendCell(aggregation.label);
			row.appendCell(aggregation.interval);
			row.appendCell(aggregation.type);

			row.appendCell().appendButton("View stream", function() {
				console.log("View for aggregation", aggregation);

				screenController.showScreen("aggregationStream", {
					aggregation : aggregation
				});
			});
			
			row.appendCell().appendButton("View chart", function() {
				screenController.showScreen("aggregationChart", {
					aggregation : aggregation
				});
			});

		};

		var addButton = addButtonCell.appendButton("Add", function() {
			statusCell.target().text("");

			_this.api.createAggregation(
					patternInput.target().val(), 
					labelInput.target().val(),
					intervalInput.target().val(),
					typeInput.target().val(), function(result) {
				console.log(result);

				if (result.state == "Successful") {
					var aggregation = result.value;

					rowMapper(aggregation);

					labelInput.target().effect("highlight", {
						color : "#00ff00"
					}, 500);
					patternInput.target().effect("highlight", {
						color : "#00ff00"
					}, 500);

				} else {

					statusCell.target().text(result.externalReason);

					labelInput.target().effect("highlight", {
						color : "#ff0000"
					}, 500);
					patternInput.target().effect("highlight", {
						color : "#ff0000"
					}, 500);
				}
			});
		});

		this.api.getAggregations(function(result) {
			var aggregations = result.value || [];
			for (var i = 0; i < aggregations.length; i++) {
				var aggregation = aggregations[i];
				rowMapper(aggregation);
			}
		});

	};
};


function MainScreen(api) {
	this.api = api;

	this.allEvents = [];
	this.pausedEvents = [];

	this.table;
	this.tableDiv;

	this.eventViewDiv;

	this.isPlaying = true;
	this.playPause;

	this.time = 0;

	this.staticTable = undefined;

	this.levelColours = {
		finest : "#6B8ADF",
		finer : "#92B8E4",
		fine : "#A0CCFF",
		info : "#F7F7FE",
		config : "#E2E2FE",
		warning : "#F9F915",
		severe : "#FF484C"
	};

	/*
	 * This allows us to detect non-automated scroll events
	 */
	this.ignoreScrollEvent = false;

	this.borderLayout = undefined;

	this.cleanup = function() {
		this.api.unsubscribe("events");
	};
	
	this.render = function(screenController, builder, parameters) {
		screenController.setSessionToken("sessionToken");

		var scrollerStyle = 'background-color: #F5F6F7; border: 1px solid #dfdfdf; padding: 0px;';

		var pstyle = 'background-color: #F5F6F7; border: 1px solid #dfdfdf; padding: 1px;';

		var toolbarDiv = builder.appendDiv();

		 $().w2destroy('toolbar');
		 $().w2destroy('scrollLayout');
		 $().w2destroy('tableLayout');
		 $().w2destroy('layout');
		
		toolbarDiv.target().w2toolbar({
			name : 'toolbar',
			onClick : function(target, info) {
				console.log(target, info);
				if (info.item.id == 'item5') {
					screenController.showScreen("patterns");
				}else if (info.item.id == 'aggregations') {
					screenController.showScreen("aggregations");
				} 
			},
			items : [              
			         {  type : 'button',id : 'item5', caption : 'View Patterns'	}, 
			         { type : 'break', id : 'break2' },
			         {  type : 'button',id : 'aggregations', caption : 'View Aggregations'	},
			]
		});

		var mainDiv = builder.appendDiv();
		mainDiv.target().css("height", "100%");

		var scrollDiv = mainDiv.appendDiv();
		scrollDiv.target().w2layout({
			name : 'scrollLayout',
			panels : [ {
				type : 'top',
				size : 50,
				resizable : false,
				style : scrollerStyle,
				content : ''
			}, {
				type : 'main',
				style : scrollerStyle,
				content : ''
			}, {
				type : 'bottom',
				size : 50,
				resizable : false,
				style : scrollerStyle,
				content : ''
			} ]
		});

		var tableDiv = mainDiv.appendDiv();
		tableDiv.target().w2layout({
			name : 'tableLayout',
			panels : [ {
				type : 'main',
				style : pstyle,
				content : ''
			}, {
				type : 'right',
				size : 50,
				resizable : false,
				style : pstyle,
				content : 'right'
			}, ]
		});

		mainDiv.target().w2layout({
			name : 'layout',
			panels : [ {
				type : 'top',
				size : 34,
				resizable : false,
				style : pstyle,
				content : ''
			}, {
				type : 'main',
				style : pstyle
			}, {
				type : 'preview',
				size : '50%',
				resizable : true,
				style : pstyle,
				content : 'preview'
			}, {
				type : 'bottom',
				size : 50,
				resizable : false,
				style : pstyle,
				content : 'bottom'
			} ]
		});

		w2ui['layout'].content('main', w2ui['tableLayout']);

		w2ui['tableLayout'].content('right', w2ui['scrollLayout']);

		w2ui['scrollLayout'].content('top', "<img src='Up.png'/>");
		w2ui['scrollLayout'].content('bottom', "<img src='Down.png'/>");

		var topContainerDiv = $("#view #layout_layout_panel_top .w2ui-panel-content");
		console.log("top container div", topContainerDiv);
		var topBuilder = Builder(topContainerDiv);
		topContainerDiv.css("overflow", "hidden");

		var levelFilter = topBuilder.appendSelect([ "Severe", "Warning", "Info", "Fine", "Finer", "Finest" ], [ LEVEL_SEVERE, LEVEL_WARNING, LEVEL_INFO, LEVEL_FINE, LEVEL_FINER, LEVEL_FINEST ],
				LEVEL_INFO, function(selected) {
					_this.staticTable.levelFilter.setLevel(levelFilter.target().val());
					_this.staticTable.refilter();
				});

		var quickFilter = topBuilder.appendInput();

		this.playPause = topBuilder.appendImage("Pause.png");
		this.playPause.target().css("vertical-align", "middle");

		this.playPause.target().click(function() {
			_this.staticTable.togglePlaying();
			if (_this.staticTable.isPlaying) {
				_this.playPause.target().attr("src", "Pause.png");
			} else {
				_this.playPause.target().attr("src", "Play.png");
			}

		});

		var clear = topBuilder.appendImage("Clear2.png");
		clear.target().css("vertical-align", "middle");

		clear.target().click(function() {
			_this.staticTable.clear();
			if (_this.staticTable.isPlaying) {
				_this.playPause.target().attr("src", "Pause.png");
			} else {
				_this.playPause.target().attr("src", "Play.png");
			}
		});

		this.eventViewDiv = Builder($("#view #layout_layout_panel_preview .w2ui-panel-content"));

		var innerMiddleDiv = $("#view #layout_tableLayout_panel_main .w2ui-panel-content");
		var tableBuilder = Builder(innerMiddleDiv);
		this.staticTable = new StaticTable(tableBuilder);

		// Setup the scroller clicks
		var scrollerTopDiv = $("#layout_scrollLayout_panel_top .w2ui-panel-content");
		scrollerTopDiv.click(function() {
			_this.staticTable.scrollUp();
		});

		var scrollerBottomDiv = $("#layout_scrollLayout_panel_bottom .w2ui-panel-content");
		scrollerBottomDiv.click(function() {
			_this.staticTable.scrollDown();
		});

		// Suscribe to the event stream

		var _this = this;

		api.subscribe("events", {
			onSuccessful : function(response) {
				// output.appendDiv(JSON.stringify(response));
			},
			onUnsuccessful : function(response) {
				// output.appendDiv(JSON.stringify(response));
			},
			onFailed : function(response) {
				// output.appendDiv(JSON.stringify(response));
			}
		}, function(event) {
			_this.staticTable.addEvent(event);
			if (_this.staticTable.isPlaying) {
				_this.staticTable.scrollToBottom();
			}
		});

		// Setup the timer thats going to process the keystrokes
		var timer = $.timer(function() {
			var quickFilterText = quickFilter.target().val();
			_this.staticTable.containsFilter.setString(quickFilterText);
			_this.staticTable.refilter();
			this.stop();
		}, 200, false);

		quickFilter.target().keyup(function() {
			console.log("Starting timer");
			timer.stop();
			timer.play(true);
		});

		// Bind to table clicks
		this.staticTable.selectionHandlers.push(function(event) {
			_this.showEventDetails(event);
		});

	}
//
//	this.render_fail = function(screenController, builder, parameters) {
//		var _this = this;
//
//		screenController.setSessionToken("sessionToken");
//
//		// $().w2destroy('toolbar');
//		// $().w2destroy('scrollLayout');
//		// $().w2destroy('tableLayout');
//		// $().w2destroy('layout');
//
//		var scrollerStyle = 'background-color: #F5F6F7; border: 1px solid #dfdfdf; padding: 0px;';
//
//		var pstyle = 'background-color: #F5F6F7; border: 1px solid #dfdfdf; padding: 1px;';
//
//		// var toolbarDiv = builder.appendDiv();
//		//		
//		// toolbarDiv.target().w2toolbar({
//		// name: 'toolbar',
//		// onClick: function (target, info) {
//		// console.log(target, info);
//		// if (info.item.id == 'item5') {
//		// screenController.showScreen("patterns");
//		// }
//		// },
//		// items: [
//		// { type: 'check', id: 'item1', caption: 'Check', img: 'icon-page',
//		// checked: true },
//		// { type: 'break', id: 'break0' },
//		// { type: 'menu', id: 'item2', caption: 'Drop Down', img:
//		// 'icon-folder', items: [
//		// { text: 'Item 1', icon: 'icon-page' },
//		// { text: 'Item 2', icon: 'icon-page' },
//		// { text: 'Item 3', value: 'Item Three', icon: 'icon-page' }
//		// ]},
//		// { type: 'break', id: 'break1' },
//		// //{ type: 'radio', id: 'item3', group: '1', caption: 'Radio 1', icon:
//		// 'fa-star', checked: true },
//		// //{ type: 'radio', id: 'item4', group: '1', caption: 'Radio 2', icon:
//		// 'fa-star-empty' },
//		// { type: 'button', id: 'item5', caption: 'Patterns' }
//		// ]
//		// });
//
//		var mainDiv = builder;// .appendDiv();
//		// mainDiv.target().css("height", "90%");
//
//		var scrollDiv = builder.appendDiv();
//		scrollDiv.target().w2layout({
//			name : 'scrollLayout',
//			panels : [ {
//				type : 'top',
//				size : 50,
//				resizable : false,
//				style : scrollerStyle,
//				content : ''
//			}, {
//				type : 'main',
//				style : scrollerStyle,
//				content : ''
//			}, {
//				type : 'bottom',
//				size : 50,
//				resizable : false,
//				style : scrollerStyle,
//				content : ''
//			} ]
//		});
//
//		var tableDiv = builder.appendDiv();
//		tableDiv.target().w2layout({
//			name : 'tableLayout',
//			panels : [ {
//				type : 'main',
//				style : pstyle,
//				content : ''
//			}, {
//				type : 'right',
//				size : 50,
//				resizable : false,
//				style : pstyle,
//				content : 'right'
//			}, ]
//		});
//
//		var layoutDiv = builder.appendDiv();
//		layoutDiv.target().w2layout({
//			name : 'layout',
//			panels : [ {
//				type : 'top',
//				size : 34,
//				resizable : false,
//				style : pstyle,
//				content : ''
//			}, {
//				type : 'main',
//				style : pstyle
//			}, {
//				type : 'preview',
//				size : '50%',
//				resizable : true,
//				style : pstyle,
//				content : 'preview'
//			}, {
//				type : 'bottom',
//				size : 50,
//				resizable : false,
//				style : pstyle,
//				content : 'bottom'
//			} ]
//		});
//
//		w2ui['layout'].content('main', w2ui['tableLayout']);
//
//		w2ui['tableLayout'].content('right', w2ui['scrollLayout']);
//
//		w2ui['scrollLayout'].content('top', "<img src='up.png'/>");
//		w2ui['scrollLayout'].content('bottom', "<img src='down.png'/>");
//
//		var topContainerDiv = $("#view #layout_layout_panel_top .w2ui-panel-content");
//		console.log("top container div", topContainerDiv);
//		var topBuilder = Builder(topContainerDiv);
//		topContainerDiv.css("overflow", "hidden");
//
//		var levelFilter = topBuilder.appendSelect([ "Severe", "Warning", "Info", "Fine", "Finer", "Finest" ], [ LEVEL_SEVERE, LEVEL_WARNING, LEVEL_INFO, LEVEL_FINE, LEVEL_FINER, LEVEL_FINEST ],
//				LEVEL_INFO, function(selected) {
//					_this.staticTable.levelFilter.setLevel(levelFilter.target().val());
//					_this.staticTable.refilter();
//				});
//
//		var quickFilter = topBuilder.appendInput();
//
//		this.playPause = topBuilder.appendImage("Pause.png");
//		this.playPause.target().css("vertical-align", "middle");
//
//		this.playPause.target().click(function() {
//			_this.staticTable.togglePlaying();
//			if (_this.staticTable.isPlaying) {
//				_this.playPause.target().attr("src", "Pause.png");
//			} else {
//				_this.playPause.target().attr("src", "Play.png");
//			}
//
//		});
//
//		var clear = topBuilder.appendImage("Clear2.png");
//		clear.target().css("vertical-align", "middle");
//
//		clear.target().click(function() {
//			_this.staticTable.clear();
//			if (_this.staticTable.isPlaying) {
//				_this.playPause.target().attr("src", "Pause.png");
//			} else {
//				_this.playPause.target().attr("src", "Play.png");
//			}
//		});
//
//		this.eventViewDiv = Builder($("#view #layout_layout_panel_preview .w2ui-panel-content"));
//
//		var innerMiddleDiv = $("#view #layout_tableLayout_panel_main .w2ui-panel-content");
//		var tableBuilder = Builder(innerMiddleDiv);
//		this.staticTable = new StaticTable(tableBuilder);
//
//		// Setup the scroller clicks
//		var scrollerTopDiv = $("#layout_scrollLayout_panel_top .w2ui-panel-content");
//		scrollerTopDiv.click(function() {
//			_this.staticTable.scrollUp();
//		});
//
//		var scrollerBottomDiv = $("#layout_scrollLayout_panel_bottom .w2ui-panel-content");
//		scrollerBottomDiv.click(function() {
//			_this.staticTable.scrollDown();
//		});
//
//		// Suscribe to the event stream
//		api.subscribe("events", {
//			onSuccessful : function(response) {
//				// output.appendDiv(JSON.stringify(response));
//			},
//			onUnsuccessful : function(response) {
//				// output.appendDiv(JSON.stringify(response));
//			},
//			onFailed : function(response) {
//				// output.appendDiv(JSON.stringify(response));
//			}
//		}, function(event) {
//			// console.log(event);
//			_this.staticTable.addEvent(event);
//			if (_this.staticTable.isPlaying) {
//				_this.staticTable.scrollToBottom();
//			}
//		});
//
//		// Setup the timer thats going to process the keystrokes
//		var timer = $.timer(function() {
//			var quickFilterText = quickFilter.target().val();
//			_this.staticTable.containsFilter.setString(quickFilterText);
//			_this.staticTable.refilter();
//			this.stop();
//		}, 200, false);
//
//		quickFilter.target().keyup(function() {
//			console.log("Starting timer");
//			timer.stop();
//			timer.play(true);
//		});
//
//		// Bind to table clicks
//		this.staticTable.selectionHandlers.push(function(event) {
//			_this.showEventDetails(event);
//		});
//
//	};

//	this.renderOld = function(screenController, builder, parameters) {
//		$.cookie('view', 'main');
//
//		var _this = this;
//
//		this.borderLayout = new BorderLayout($(window), false);
//
//		var top = builder.appendDiv();
//		// var left = builder.appendDiv("Left");
//		// var right = builder.appendDiv("Right");
//		var bottom = builder.appendDiv("Bottom");
//		var middle = builder.appendDiv("Middle");
//
//		this.borderLayout.setTop(top.target());
//		// this.borderLayout.setLeft(left.target(), "20%");
//		// this.borderLayout.setRight(right.target(), "40%");
//		this.borderLayout.setBottom(bottom.target());
//		this.borderLayout.setMiddle(middle.target());
//
//		var eventsDiv = middle.appendDiv("");
//		var chartsDiv = middle.appendDiv("Charts");
//
//		chartsDiv.target().hide();
//
//		top.appendLink("Events", "#", function() {
//			console.log("Showing events");
//			eventsDiv.target().show();
//			chartsDiv.target().hide();
//		});
//
//		top.appendLink("Charts", "#", function() {
//			console.log("Showing charts");
//			eventsDiv.target().hide();
//			chartsDiv.target().show();
//		});
//
//		top.appendLink("Test", "#", function() {
//			_this.runTests();
//		});
//
//		top.appendLink("Add event (new)", "#", function() {
//
//			var events = 1000;
//
//			for (var i = 0; i < events; i++) {
//				var event = {
//					message : "Message-0",
//					level : 800,
//					levelDescription : "INFO",
//					time : _this.time
//				};
//
//				_this.time += 1000;
//
//				_this.staticTable.addEvent(event);
//			}
//
//			_this.staticTable.scrollToBottom();
//		});
//
//		var canvas = chartsDiv.appendCanvas();
//		var context = canvas.target().get(0).getContext("2d");
//		var data = {
//			labels : [ "January", "February", "March", "April", "May", "June", "July" ],
//			datasets : [ {
//				fillColor : "rgba(220,220,220,0.5)",
//				strokeColor : "rgba(220,220,220,1)",
//				pointColor : "rgba(220,220,220,1)",
//				pointStrokeColor : "#fff",
//				data : [ 65, 59, 90, 81, 56, 55, 40 ]
//			}, {
//				fillColor : "rgba(151,187,205,0.5)",
//				strokeColor : "rgba(151,187,205,1)",
//				pointColor : "rgba(151,187,205,1)",
//				pointStrokeColor : "#fff",
//				data : [ 28, 48, 40, 19, 96, 27, 100 ]
//			} ]
//		}
//		var options = {};
//
//		new Chart(context).Line(data, options);
//
//		var topBit = top.appendDiv("");
//
//		var levelFilter = topBit.appendSelect([ "Severe", "Warning", "Info", "Fine", "Finer", "Finest" ], [ LEVEL_SEVERE, LEVEL_WARNING, LEVEL_INFO, LEVEL_FINE, LEVEL_FINER, LEVEL_FINEST ],
//				LEVEL_INFO, function(selected) {
//					_this.staticTable.levelFilter.setLevel(levelFilter.target().val());
//					_this.staticTable.refilter();
//				});
//
//		var quickFilter = topBit.appendInput();
//		this.playPause = topBit.appendImage("Pause.png");
//
//		this.playPause.target().click(function() {
//			_this.setPlaying(!_this.isPlaying);
//		});
//
//		var clear = topBit.appendImage("Clear.png");
//		clear.target().click(function() {
//			_this.staticTable.clear();
//		});
//
//		this.tableDiv = eventsDiv.appendDiv();
//		this.tableDiv.target().css("height", $(window).height() * 0.6);
//
//		this.staticTable = new StaticTable(middle);
//
//		this.borderLayout.addLayoutListener(function() {
//			_this.staticTable.doLayout();
//		});
//
//		// this.tableDiv.target().css("overflow", "auto");
//		// tableDiv.target().css("background-color", "red");
//
//		this.eventViewDiv = eventsDiv.appendDiv();
//		var bottomBit = eventsDiv.appendDiv();
//
//		// this.table = this.tableDiv.appendTable();
//		// this.table.target().css("overflow-y", "scroll");
//
//		api.subscribe("events", {
//			onSuccessful : function(response) {
//				// output.appendDiv(JSON.stringify(response));
//			},
//			onUnsuccessful : function(response) {
//				// output.appendDiv(JSON.stringify(response));
//			},
//			onFailed : function(response) {
//				// output.appendDiv(JSON.stringify(response));
//			}
//		}, function(event) {
//			_this.staticTable.addEvent(event);
//			_this.staticTable.scrollToBottom();
//		});
//
//		// Setup the timer thats going to process the keystrokes
//		var timer = $.timer(function() {
//			var quickFilterText = quickFilter.target().val();
//			_this.staticTable.containsFilter.setString(quickFilterText);
//			_this.staticTable.refilter();
//			this.stop();
//		}, 200, false);
//
//		quickFilter.target().keyup(function() {
//			console.log("Starting timer");
//			timer.stop();
//			timer.play(true);
//		});
//
//	};

//	this.runTests = function() {
//
//		var time = 0;
//		var events = 1;
//		var event = {
//			message : "Message-0",
//			level : 800,
//			levelDescription : "INFO",
//			time : 0
//		};
//
//		// for(var i = 0; i < events; i++) {
//		// event.message = "Message-" + i;
//		// event.time = time + i;
//		// this.addEvent(event);
//		// }
//
//		var suite = new Benchmark.Suite;
//
//		var counter = 0;
//
//		var _this = this;
//		// add tests
//		suite.add('Table test', function() {
//			event.message = "Message-" + counter++;
//			event.time = time += 1000;
//			_this.addEvent(event);
//		}).add('Static table', function() {
//			event.message = "Message-" + counter++;
//			event.time = time += 1000;
//			_this.staticTable.addEvent(event);
//			_this.staticTable.scrollToBottom();
//		})
//		// .add('String#match', function() {
//		// !!'Hello World!'.match(/o/);
//		// })
//		// add listeners
//		.on('cycle', function(event) {
//			console.log(String(event.target));
//		}).on('complete', function() {
//			console.log('Fastest is ' + this.filter('fastest').pluck('name'));
//		})
//		// run async
//		.run({
//			'async' : true
//		});
//
//	};

	this.addEvent = function(event) {
		// Add to all events so we can bring it back if it doesn't pass the
		// filter
		this.allEvents.push(event);

		if (this.isPlaying) {
			// Does it pass the filter?
			if (this.filter.passes(event)) {
				this.addRow(event);
				this.tableDiv.target().scrollTo('max');
			}
		} else {
			this.pausedEvents.push(event);
		}
	}

	this.showEventDetails = function(event) {
		this.eventViewDiv.target().empty();

		var div = this.eventViewDiv.appendDiv();

		var top = div.appendDiv();

		top.target().css("background-color", "#ddeeff");

		var topLeft = top.appendDiv();

		var topLeftTable = topLeft.appendTable();
		topLeftTable.buildRow([ "Recieved timestamp", formatTime(event.time) ]);
		topLeftTable.buildRow([ "Source Application", event.sourceApplication ]);
		topLeftTable.buildRow([ "Class", event.sourceClassName ]);
		topLeftTable.buildRow([ "Thread", event.threadName ]);

		var topRight = top.appendDiv();
		topLeft.target().css("float", "left");

		var topRightTable = topRight.appendTable();
		topRightTable.buildRow([ "Level", event.levelDescription ]);
		topRightTable.buildRow([ "Source host", event.sourceHost ]);
		topRightTable.buildRow([ "Method", event.sourceMethodName ]);
		topRightTable.buildRow([ "Logger", event.loggerName ]);

		var middle = div.appendDiv(event.message);

		var bottom = div.appendDiv();

	};
}

formatTime = function(time) {
	var date = new Date(time);

	var day = date.getDate();
	var month = date.getMonth() + 1;

	var formatted = this.formatPad(day) + "-" + this.formatPad(month) + "-" + date.getFullYear() + " " + this.formatPad(date.getHours()) + ":" + this.formatPad(date.getMinutes()) + ":"
			+ this.formatPad(date.getSeconds()) + "." + date.getMilliseconds();

	return formatted;
};

formatPad = function(value) {
	var formatted = "";
	if (value < 10) {
		formatted += "0";
	}

	formatted += value;
	return formatted;
};

function LogonScreen(api) {

	this.api = api;

	this.render = function(screenController, builder) {
		var formDiv = builder.subdiv();

		formDiv.appendSpan("Username");
		var usernameInput = formDiv.appendInput("username", "admin");

		formDiv.appendSpan("Password");
		var passwordInput = formDiv.appendPassword("password", "password");

		var button = formDiv.appendButton("Logon");

		var errorDiv = builder.appendDiv();

		button.target().on("click", function() {
			api.logon(usernameInput.target().val(), passwordInput.target().val(), function(data) {
				var sessionToken = jQuery.parseJSON(data);
				if (sessionToken.success) {
					screenController.setSessionToken(sessionToken);
					screenController.showDefaultScreen();
					// global_session = obj;
					// showMainScreen(builder);
				} else {
					errorDiv.target().text(sessionToken.reason);
				}
			});
		});
	};
}
