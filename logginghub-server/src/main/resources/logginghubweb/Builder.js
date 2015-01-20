/**
 * Builder is a simple jquery enabled wrapper around a div that allows us to quickly build nested elements dynamically.	
 * 
 * Its using the "constructor function returning 'that' reference" method of class definitions... trying out the different styles to work out pros and cons.	
 */

var Builder = function(div) {
	
	var wrappedDiv = div;
    var that = {};

    that.target = function() {
        return wrappedDiv;
    };
    
    that.subdiv = function() {
    	var dynamicDiv = $('<div/>', {});
        wrappedDiv.append(dynamicDiv);
        return Builder(dynamicDiv);
    };
    
    that.subSpan= function() {
    	var span = $('<span/>', {});
        wrappedDiv.append(span);
        return Builder(span);
    };
    
    that.appendDiv = function(contents) {
    	var dynamicDiv = $('<div/>', {
    		text: contents
    	});
        wrappedDiv.append(dynamicDiv);
        return Builder(dynamicDiv);
    };
    
    that.appendSubmit = function(text) {
    	var dynamic = $('<input/>', {
    		type: "submit",
    		value: text
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    
    that.appendSpan = function(contents) {
    	var dynamicDiv = $('<span/>', {
    		text: contents
    	});
        wrappedDiv.append(dynamicDiv);
        return Builder(dynamicDiv);
    };
    
    that.appendTable = function(contents) {
    	var dynamic = $('<table/>', {
    		text: contents
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendImage = function(src) {
    	var dynamic = $('<img/>', {
    		src: src
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendH1 = function(contents) {
    	var dynamic = $('<h1/>', {
    		text: contents
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendTableHeader = function() {
    	var dynamic = $('<thead/>', {
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendHeaderCell = function(contents) {
    	var dynamic = $('<th/>', {
    		text: contents
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendTableBody = function() {
    	var dynamic = $('<tbody/>', {
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendTableFooter= function() {
    	var dynamic = $('<tfoot/>', {
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    
    that.appendForm = function() {
    	var dynamic = $('<form/>', {
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendImage = function(src) {
    	var dynamic = $('<img/>', {
    		src: src
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendCanvas = function() {
    	var dynamic = $('<canvas/>', {
    		width: 400,
    		height: 400
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendRow = function() {
    	var dynamic = $('<tr/>', {
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.buildRow = function(items) {
    	var dynamic = $('<tr/>', {
    	});
    	
        wrappedDiv.append(dynamic);               
        var builder =  Builder(dynamic);
        
        for(var i = 0; i < items.length; i++) {
        	var item = items[i];
        	builder.appendCell(item);
        }
        
        return builder;
    };
    
    that.appendCell = function(contents) {
    	var dynamic = $('<td/>', {
    		text: contents
    	});
        wrappedDiv.append(dynamic);
        return Builder(dynamic);
    };
    
    that.appendButton = function(contents, handler) {
    	var dynamic = $('<button/>', {
    		text: contents
    	});
        wrappedDiv.append(dynamic);
        
        dynamic.click(handler);
        
        return Builder(dynamic);
    };
    
    that.appendSelect= function(labels, values, defaultValue, handler) {
    	var dynamic = $('<select/>', {    		
    	});
    	
    	$.each(labels, function (index, value) {
    		var properties = { 
        	        value: values[index],
        	        text : value 
        	    };
    		
    		if(values[index] == defaultValue) {
    			properties.selected = true;	
    		}
    		
    	    dynamic.append($('<option/>', properties));
    	});   
    	
        wrappedDiv.append(dynamic);
        dynamic.change(handler);
        return Builder(dynamic);
    };
    
    that.appendLink = function(text, href, handler) {
    	var dynamic = $('<a/>', {
    		href: href,
    		text: text
    	});
        wrappedDiv.append(dynamic);
        
        dynamic.click(handler);
        
        return Builder(dynamic);
    };
    
    that.appendText = function(text) {
    	console.log("Appending " + text + " to wrapped div");
        wrappedDiv.append(text);
    };
    
    
    
    that.appendInput = function(name, value) {
    	var input = jQuery('<input/>', {
    	    type: 'text',
    	    name: name,
    	    value: value
    	});
    	
    	input.appendTo(wrappedDiv);
    	
    	return Builder(input);
    };

    that.appendPassword = function(name, value) {
    	var input = jQuery('<input/>', {
    	    type: 'password',
    	    name: name,
    	    value: value
    	});
    	
    	input.appendTo(wrappedDiv);
    	
    	return  Builder(input);
    };

    return that;
};
