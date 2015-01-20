

function BorderLayoutTest() {

}

BorderLayoutTest.prototype.initialise = function(div) {
	
	this.builder = Builder(div);
	
	this.borderLayout = new BorderLayout($(window), false);
	
	var top = this.builder.appendDiv("Top");
	var left = this.builder.appendDiv("Left");
	var right = this.builder.appendDiv("Right");
	var bottom = this.builder.appendDiv("Bottom");
	var middle = this.builder.appendDiv("Middle");
	
	this.borderLayout.setTop(top.target());
	this.borderLayout.setLeft(left.target(), "20%");
	this.borderLayout.setRight(right.target(), "40%");
	this.borderLayout.setBottom(bottom.target());
	this.borderLayout.setMiddle(middle.target());
	
	this.innerBorderLayout = new BorderLayout(middle.target(), true);
	
	var top2 = this.builder.appendDiv("Top2");
	var left2 = this.builder.appendDiv("Left2");
	var right2 = this.builder.appendDiv("Right2");
	var bottom2 = this.builder.appendDiv("Bottom2");
	var middle2 = this.builder.appendDiv("Middle2");
	
	this.innerBorderLayout.setTop(top2.target());
	this.innerBorderLayout.setLeft(left2.target(), "20");
	this.innerBorderLayout.setRight(right2.target(), "40");
	this.innerBorderLayout.setBottom(bottom2.target());
	this.innerBorderLayout.setMiddle(middle2.target());
	
	
	
};


function BorderLayout(target, allowRelative, name) {
	
	this.name = name;
	this.target = target;	
	this.containerWidth = 0;
	this.containerHeight = 0;
	this.allowRelative = allowRelative;
	this.listeners =  [];
	
	var _this = this;
	
	target.on("resize", function() {					   
	    _this.doLayout();
	});
	
	
	
}

BorderLayout.prototype.addLayoutListener = function(listener) {
	this.listeners.push(listener);
};

BorderLayout.prototype.doLayout = function(element) {
	
	this.containerHeight = this.target.height();
	this.containerWidth = this.target.width();
//	console.log(this.name + ":: container is", this.target);
    console.log(this.name + ":: container height is " + this.containerWidth + ", " + this.containerHeight);
	
	// Start from the top left and layout right and down
	var x = 0;
	var y = 0;
	
	
    if(this.allowRelative) {
    	x = this.target.position().left;
    	y = this.target.position().top;
    	console.log(this.name + ":: relative x " + x + " y " + y);
    }
    
    
	var midX = 0;
	var midWidth = this.containerWidth;
	var midY = 0;
	var midHeight = this.containerHeight;	
	
	if(this.top) {
		this.top.css({left: x, top: y, width: this.containerWidth});
		midY = this.top.height();
	}
	
//	console.log("MidX=" + midX + " midY=" + midY + " midHeight=" + midHeight + " midWidth=" + midWidth);
	
	if(this.bottom) {		
		var bottomX = x;
		var bottomY = y + this.containerHeight - this.bottom.height();

//		log(["y", "containerHeight", "bottom.hieght"], [ y, this.containerHeight, this.bottom.height()]);
//		console.log("BottomX=" + bottomX + " bottomY=" + bottomY);
		
		this.bottom.css({left: bottomX, top: bottomY, width: this.containerWidth});
		midHeight = this.containerHeight - this.bottom.height() - midY;
	}
	
//	console.log("MidX=" + midX + " midY=" + midY + " midHeight=" + midHeight + " midWidth=" + midWidth);
	
	if(this.left) {
		this.left.css({left: x, top: y+  midY, height: midHeight});
		midX = this.left.width();
	}
		
//	console.log("MidX=" + midX + " midY=" + midY + " midHeight=" + midHeight + " midWidth=" + midWidth);
	
	if(this.right) { 
		
		var rightX = x + this.containerWidth - this.right.width();
		var rightY = y + midY;
		
//		log(["x", "this.containerWidth", "this.right.width()"], [ x,this.containerWidth, this.right.width()]);
		
		this.right.css({left: rightX, top: rightY, height: midHeight });
		
		midWidth = this.containerWidth - this.right.width() - midX;	
	}
	
//	console.log("MidX=" + midX + " midY=" + midY + " midHeight=" + midHeight + " midWidth=" + midWidth);
	
	if(this.middle) {
		console.log(this.name + ":: MidX=" + midX + " midY=" + midY + " midWidth=" + midWidth+ " midHeight=" + midHeight);
//		console.log(this.name + ":: mid is ", this.middle);
		this.middle.css({left: x+  midX, top: y + midY, width: midWidth, height: midHeight });
	}
	
	for(var i = 0; i < this.listeners.length; i++) {
		this.listeners[i]();
	}
};

log = function(descriptions, values) {
	
	var message = "";
	for(var i = 0; i < descriptions.length; i++) {
		message += descriptions[i] + " = '" + values[i] + "' ";
	}

	console.log(message);
}

BorderLayout.prototype.setTop = function(element, height) {
	element.css("position", "absolute");
	element.css("background-color", "red");
	element.css("height", height);
	this.top = element;	
	this.doLayout();
};

BorderLayout.prototype.setLeft= function(element, width) {
	element.css("position", "absolute");
	element.css("background-color", "green");
	element.css("width", width);
	this.left = element;
	this.doLayout();
};

BorderLayout.prototype.setRight = function(element, width) {
	element.css("position", "absolute");
	element.css("background-color", "blue");
	element.css("width", width);
	this.right = element;
	this.doLayout();
};

BorderLayout.prototype.setBottom = function(element, height) {
	element.css("position", "absolute");
	element.css("background-color", "yellow");
	element.css("height", height);
	this.bottom = element;
	this.doLayout();
};

BorderLayout.prototype.setMiddle = function(element) {
	element.css("position", "absolute");
	element.css("background-color", "grey");
	this.middle = element;
	this.doLayout();
};