/**
 * 
 */

function StaticTable(builder)  {
	var _this = this;
	this.rowCount = 10;
	
	this.levelColours = {
			finest : "#6B8ADF",
			finer : "#92B8E4",
			fine : "#A0CCFF",
			info : "#F7F7FE",
			config : "#E2E2FE",
			warning : "#F9F915",
			severe : "#FF484C"
		};
	
	this.tableHeight = null;
	
//	this.borderLayout = new BorderLayout(builder.target(), false, "StaticTable");
	
//	var top = builder.appendDiv("T:Top");
//	var left = builder.appendDiv("T:Left");
//	var right = builder.appendDiv("T:Right");
//	var bottom = builder.appendDiv("T:Bottom");
//	this.middle= builder.appendDiv();
//	this.middle.target().attr("id", "middlediv");
//	this.middle.target().css("height", "100%");
	
//	this.borderLayout.setTop(top.target());
//	this.borderLayout.setLeft(left.target(), "20%");
//	this.borderLayout.setRight(right.target(), "100");
//	this.borderLayout.setBottom(bottom.target());
//	this.borderLayout.setMiddle(this.middle.target());
	
//	var mainDiv = builder.appendDiv();
//	mainDiv.target().css("float", "left");
	
//	var buttons = mainDiv.appendDiv();
	
//	buttons.appendButton("Up", function() {
//		_this.scrollUp();	
//	});
//	
//	buttons.appendButton("Down", function() {
//		_this.scrollDown();	
//	});
	
	this.tableDiv = builder.appendDiv();
	this.tableDiv.target().css("height", "100%");
	
	this.table = this.tableDiv.appendTable();
	this.tableHeader = this.tableDiv.appendTableHeader();
	this.tableBody = this.tableDiv.appendTableBody();
	
	this.table.target().css("table-layout", "fixed");
	
	var headerRow = this.tableHeader.appendRow();
	
	var timeHeader = headerRow.appendHeaderCell("Time");
	var sourceHeader = 	headerRow.appendHeaderCell("Source Application");
	var levelHeader = headerRow.appendHeaderCell("Level");
	var messageHeader = headerRow.appendHeaderCell("Message");
	
	timeHeader.target().attr("width", "280");
	sourceHeader.target().attr("width", "200");
	levelHeader.target().attr("width", "180");
	messageHeader.target().attr("width", "2000");
	
	
	
//	this.scrollerDiv = right.appendDiv("Scroller");
	
//	this.scrollerBubble = builder.appendDiv("Bubble");
//	this.scrollerBubble.target().height(20);
//	this.scrollerBubble.target().width(20);
//	this.scrollerBubble.target().css("background-color", "grey");
//	this.scrollerBubble.target().css("position", "relative");
	
//	this.scrollerUp = builder.appendButton("U");
//	this.scrollerUp.target().css("position", "relative");
//	this.scrollerUp.target().css({left: 0, top: 0});
//	this.scrollerUp.target().width(20);
//	this.scrollerUp.target().height(20);
	
	
//	this.scrollerDown = builder.appendButton("D");
//	this.scrollerDown.target().css("position", "relative");
//	this.scrollerDown.target().height(20);
//	this.scrollerDown.target().width(20);
	
//	this.scrollerBorderLayout = new BorderLayout(right.target(), true, "Scroller");	
//	scrollerBorderLayout.setTop(this.scrollerUp.target());
//	scrollerBorderLayout.setMiddle(this.scrollerBubble.target());
//	scrollerBorderLayout.setBottom(this.scrollerDown.target());
	
	
//	this.scrollerBorderLayout.setTop(this.scrollerUp.target(), "50");
//	scrollerBorderLayout.setLeft(sleft.target(), "10");
//	scrollerBorderLayout.setRight(sright.target(), "10");
//	this.scrollerBorderLayout.setBottom(this.scrollerDown.target(), "50");
//	this.scrollerBorderLayout.setMiddle(this.scrollerBubble.target());
	
//	this.scrollerDown.target().click(function()  {
//		_this.scrollDown();
//	});
//	
//	this.scrollerUp.target().click(function()  {
//		_this.scrollUp();
//	});
	
//	this.tableDiv.target().css("float", "left");
//	this.tableDiv.target().css("background-color", "green");
//	tableDiv.target().css("width", "200px");
	
//	this.scrollerDiv.target().css("float", "right");
//	this.scrollerDiv.target().css("background-color", "yellow");
//	this.scrollerDiv.target().css("width", "50px");
	
	this.visibleEvents = [];
	this.allEvents = [];
	this.pausedEvents = [];
	
	this.initialiseRows();

	this.startRow = 0;
	
	this.filter = new CompositeAndFilter();
	this.levelFilter = new LevelFilter(LEVEL_INFO);
	this.containsFilter = new EventContainsFilter();
	this.filter.addFilter(this.levelFilter);
	this.filter.addFilter(this.containsFilter);
	
	this.isPlaying = true;

	this.selectionHandlers = [];
	
};

StaticTable.prototype.onSelectedRow = function(index) {
	var actualEvent = index + this.startRow;
	var event = this.visibleEvents[actualEvent];
	console.log("Clicked row " + index + " event : " + JSON.stringify(event));
	
	if(this.selectionHandlers) {
		for(var i = 0; i < this.selectionHandlers.length; i++) {
			this.selectionHandlers[i](event);
		}
	}
};

StaticTable.prototype.initialiseRows = function() {
	
	this.tableBody.target().empty();
	
	var _this = this;
	var clickBinder = function(index) {
		return function() {
			_this.onSelectedRow(index);			
		}
	};
	
	this.rowCells = [];
	this.rows = [];
	for(var i = 0; i < this.rowCount; i++) {
		var row = this.tableBody.appendRow();
		row.target().click(clickBinder(i));
		
		this.rows[i] = row;
		
		var cells = [];
		cells.push(row.appendCell());
		cells.push(row.appendCell());
		cells.push(row.appendCell());
		cells.push(row.appendCell());
		cells.push(row.appendCell());
		cells.push(row.appendCell());
		this.rowCells.push(cells);
	}

	this.startRow = 0;
};

StaticTable.prototype.togglePlaying = function() {
	
	this.isPlaying = !this.isPlaying;
	
	if(this.isPlaying) {
		this.addPausedEvents();		
	}else{
		// Dont do anything
	}
	
};

StaticTable.prototype.addPausedEvents = function() {
	for (var i = 0; i < this.pausedEvents.length; i++) {
		var event = this.pausedEvents[i];

		var add = false;
		if (this.filter) {
			add = this.filter.passes(event);
		} else {
			add = true;
		}

		if (add) {
			this.visibleEvents.push(event);
		}
	}

	// Wacky way of clearing an array in js
	this.pausedEvents.length = 0;
};



StaticTable.prototype.clear = function() {
	
	this.visibleEvents.length =0;
	this.allEvents.length = 0;
	this.pausedEvents.length = 0;
	
	if(!this.isPlaying) {
		this.togglePlaying();
	}
	
	this.startRow = 0;
	this.render();
};

//StaticTable.prototype.doLayout = function() {
//	
//	console.log("Triggering doLayout on table");
//	
//	this.borderLayout.doLayout();
//	this.scrollerBorderLayout.doLayout();
//	
//	var tableHeight = this.middle.target().height();
//	
//	var rowHeight = 23;
//	
//	var rows = 10; //(tableHeight / rowHeight)|0; // convert to int
//	console.log("Rows ", rows);
//	this.rowCount = rows;
//	this.initialiseRows();
//	
//};



StaticTable.prototype.render = function() {
			
//	console.log("Table div is " + this.tableDiv.target().width() + " x " + this.tableDiv.target().height());

//	console.log("Last table height is " + this.tableHeight + " current is " + this.tableDiv.target().height());
	
	if(this.tableHeight) { 
		if(this.tableHeight != this.tableDiv.target().height()) {
//			console.log("Height has changed");			
			var newHeight = this.tableDiv.target().height();
			var rowHeight = 21;			
			var rows = (newHeight / rowHeight)|0; // convert to int
//			console.log("Rows ", rows);
			this.rowCount = rows;
			this.initialiseRows();
		}
	}
	
	this.tableHeight = this.tableDiv.target().height();
	
	
	// Update the scroller bar based on the current height
//	this.scrollerDiv.target().height(this.tableDiv.target().height());
	
	// Lock the down scroll button at the base of the scroller
//	this.scrollerDown.target().css({left: 0, top: this.tableDiv.target().height() - 80});	
	
	// Position the scroller bubble
//	this.scrollerBubble.target().css({left: 0, top: 80});
	
	
	var rowsToDisplay = this.rowCount;
	if(this.visibleEvents.length < rowsToDisplay) {
		rowsToDisplay = this.visibleEvents.length;
	}
//	console.log("Starting row='" + this.startRow + "' rowsToDisplay='" + rowsToDisplay + "'");
	
//	console.log("Showing from " + this.startRow + " to " + (this.startRow + rowsToDisplay));
	
	for(var i = 0; i < rowsToDisplay; i++) {
		var event = this.visibleEvents[i + this.startRow];
		
		var rowCells = this.rowCells[i];
		
//		console.log("i=", i);
		this.rows[i].target().css("background-color", this.levelColours[event.levelDescription.toLowerCase()]);
		
		rowCells[0].target().text(formatTime(event.time));
		rowCells[1].target().text(event.sourceApplication);
		rowCells[2].target().text(event.levelDescription);
		rowCells[3].target().text(event.message);
	}
	
	for(var i = rowsToDisplay; i < this.rowCount; i++) {
		var rowCells = this.rowCells[i];
		rowCells[0].target().text("");
		rowCells[1].target().text("");
		rowCells[2].target().text("");
		rowCells[3].target().text("");
		this.rows[i].target().css("background-color", "");
	}
	
};

StaticTable.prototype.scrollUp = function() {	
	
	this.startRow --;
	
	if(this.startRow < 0) {
		this.startRow = 0;
	}	
	
	this.render();
};

StaticTable.prototype.scrollDown = function() {	
	
	this.startRow ++;	
	var maximumIndex = this.visibleEvents.length - this.rowCount; 	
	
	if(this.startRow > maximumIndex) {
		this.startRow = maximumIndex;
	}
	
	this.render();
};

StaticTable.prototype.scrollToBottom = function() {	
	var maximumIndex = this.visibleEvents.length - this.rowCount;
	if(maximumIndex < 0) {
		maximumIndex = 0;
	}
	
	this.startRow = maximumIndex;
	this.render();
};

StaticTable.prototype.addEvent = function(event) {
	
	this.allEvents.push(event);

	if (this.isPlaying) {
		// Does it pass the filter?
		if (this.filter.passes(event)) {
			var index = this.visibleEvents.length;
			this.visibleEvents.push(event);
			
			if(index >= this.startRow && index < this.startRow + this.rowCount) {
				this.render();
			}
		}
	} else {
		this.pausedEvents.push(event);
	}
};

StaticTable.prototype.refilter = function() {
	console.log("Refiltering " + this.allEvents.length + " events");

	this.visibleEvents = [];
	for (var i = 0; i < this.allEvents.length; i++) {
		var event = this.allEvents[i];

		var add = false;
		if (this.filter) {
			add = this.filter.passes(event);
		} else {
			add = true;
		}

		if (add) {
			this.visibleEvents.push(event);
		}
	}
	
	// TODO : not sure what to set this to! Start or end, or previously selected row?
	this.startRow = 0;	
	
	this.render();
}

