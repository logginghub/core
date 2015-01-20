/**
 * 
 */

function EventContainsFilter(string) {
	this.string = string;
};

EventContainsFilter.prototype.setString= function(string) {
	this.string= string	;
};


EventContainsFilter.prototype.passes = function(event) {
	var passes;
	if(this.string && this.string.length > 0) {
		passes = (event.message.indexOf(this.string) > -1);
	}else{
		passes = true;		
	}	
	return passes;
};

function LevelFilter(level) {
	this.level = level;
};

LevelFilter.prototype.passes = function(event) {
	return (event.level >= this.level);
};

LevelFilter.prototype.setLevel = function(level) {
	this.level = level;
};


function CompositeAndFilter() {
	this.filters = [];
};

CompositeAndFilter.prototype.passes = function(event) {	
	var passes = true;	
	for(var i = 0; i < this.filters.length && passes; i++) {
		var filter = this.filters[i];		
		passes &= filter.passes(event);
//		console.log("Checked filter " + JSON.stringify(filter) + " against event " + JSON.stringify(event) + " : result = " + passes);
	}
	return passes;
};

CompositeAndFilter.prototype.addFilter = function(filter) {
//	console.log("This is", this);
	this.filters.push(filter);	
};

CompositeAndFilter.prototype.removeFilter = function(filter) {
	var index = this.filters.indexOf(filter);
	if(index >= 0){
		this.filters.splice(index, 1);
	}	
};

