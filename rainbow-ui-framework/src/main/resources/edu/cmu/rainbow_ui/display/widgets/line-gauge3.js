   function MovingLineGauge (placeholderName, configuration, nextDatum) {
    	this.placeholderName = placeholderName;
    	var self = this;
    	
    	this.data = [];
    	
    	this.configure = function (configuration) {
    		this.config = configuration;
    		// The width of the gauge
    		this.config.width = configuration.width || 100;
    		// The height of the gauge
    		this.config.height = configuration.height || 50;
    		this.config.graphHeight = configuration.height || 50;
    		// Whether to smooth or not: linear, basis
    		this.config.interpolation = configuration.smooth?"basis":"linear";
    		// Whether to animate the transition each interval
    		this.config.animate = configuration.animate || false;
    		// The interval for new values, ms
    		this.config.interval = configuration.interval || 1000;
    		// If animating, how long the transition should take
    		this.config.transition = configuration.transition || this.config.interval;
    		// The maximum number of values to display
    		this.config.windowSize = configuration.windowSize || Math.floor (this.config.width / 10);
    		// The minumum expected value
    		this.config.min = configuration.min || 0;
    		// The maximum expected value
			this.config.max = configuration.max || 100;
			this.config.range = this.config.max - this.config.min;
    		// Any initial data that should be displayed
    		if (this.config.initialData != null) {
    			this.data = this.config.initialData.slice (0);
    			this.max = Math.max (this.config.max, d3.max(this.data));
    			this.min = Math.min (this.config.min, d3.min(this.data));
    		}
    		else {
    			this.min = this.config.min;
    			this.max = this.config.max;
    		}
    		
    		if (this.config.label) {
    			this.config.graphHeight = this.config.graphHeight - 9;
    		}
    		
    		// Set up the x and y scales
    		// The X scale will fit windowSize values within the width of the
			// widget
    		this.xScale = d3.scale.linear ().domain ([0,this.config.windowSize]).range ([-5,this.config.width]); // starting
																													// point
																													// is
																													// -5
																													// so
																													// the
																													// first
																													// value
																													// doesn't
																													// show
																													// and
																													// slides
																													// off
																													// the
																													// edge
																													// as
																													// part
																													// of
																													// the
																													// transition
    		// The Y scale will fit values min,max within the height
    		this.yScale = d3.scale.linear ().domain([this.max, this.min]).range([0,this.config.graphHeight]);
    	}
    	this.resetYScale = function (max, min) {
    		this.max = max;
    		this.min = min;
    		this.yScale = d3.scale.linear ().domain([this.max, this.min]).range([0,this.config.graphHeight]);
			this.drawThreshold ();
    	}
    	
    	this.render = function () {
    		var div;
    		if (typeof this.placeholderName == "string") {
    			 div = d3.select("#" + this.placeholderName);
    		}
    		else {
    			div = d3.select (this.placeholderName);
    		}
    		this.group = div.append ("svg:svg").attr ("class", "movingGraphContainer").attr("width", this.config.width).attr("height",this.config.height);
    		this.graph = this.group.append ("svg:svg").attr ("width", this.config.width).attr("height", this.config.graphHeight);
    		
    		
    		// Create axes and thresholds (todo)
    		
    		// Create the line
    		this.line = d3.svg.line ()
    			// assign the X functino to plot the line
    			.x (function (d,i) {
    				return self.xScale(i);
    			})
    			.y (function (d) {
    				return self.yScale(d);
    			})
    			.interpolate (this.config.interpolation);
    		// display the line by appending an svg:path element with the data
			// line above
    		this.graph.append("svg:path").attr("class", "data").attr("d", this.line (this.data)).style("stroke", "steelblue").style ("stroke-width", "1").style ("fill","none");
    		this.yAxis = d3.svg.axis ().scale (this.yScale).orient("left");
    		var axis = this.graph.append ("g").attr("class", "y axis").call(this.yAxis)
    			.style("fill","none").style("stroke","#000").style("shape-rendering","crispEdges");
    		if (this.config.yLabel) {
    			axis.append("text").attr("transform", "rotate(-90)").attr("y", 6).attr("dy",".5em").style("font-size", "8px").style("text-anchor","end").text(this.config.yLabel);
    		}
    		if (this.config.label) {
    			this.group.append ("svg:text").attr("x", this.config.width / 2).attr ("y", this.config.height).attr("dy", "-4").attr("text-anchor", "middle").style("font-size", "9px").style("stroke-width", "1").style("fill", "#000").text (this.config.label);
    		}
    		this.group.append ("svg:text").attr ("class", "MLCurrentValue").attr("x", this.config.width - 25).attr ("y", 9).attr("text-anchor", "right").style("font-size", "8px").style("stroke-width", 1).style ("fill", "#000");
    		
    		this.drawThreshold ();
    	}
    	
    	this.drawThreshold = function () {
    		if (this.config.threshold) {
    			var threshold = this.graph.selectAll (".threshold");
    			if (threshold.empty ())
	    			this.graph.append("svg:line").attr("class","threshold").attr("x1", this.xScale (0)).attr(
							"y1", this.yScale (this.config.threshold)).attr("x2", this.xScale (this.config.width)).attr("y2",
							this.yScale(this.config.threshold)).style("stroke", "#600").style(
							"stroke-width", "1px").style("stroke-dasharray", ("3","3"));
    			else
    				threshold
    					.attr("x1", this.xScale (0)).attr(
						"y1", this.yScale(this.config.threshold)).attr("x2", this.xScale (this.config.width)).attr("y2",
								this.yScale(this.config.threshold)).style("stroke", "#600").style(
								"stroke-width", "1px").style("stroke-dasharray", ("3","3"));
    			
    		}
    	}
    	
    	
    	
    	// Starts the gauge animation (ignored if already started)
    	this.start = function () {
    		if (this.timer == null) {
	    		this.timer = setInterval (function() {
	    			var nxt = nextDatum ();
	    			if (nxt == null && self.data.length > 0) {
	    				self.data.shift ();
	    			}
	    			else if (nxt == null) {
	    				return;
	    			}
	    			else {
		    			if (self.data.length < self.config.windowSize) {
		    				self.data.push(nxt);
		    			}
		    			else {
		    				self.data.shift ();
		    				self.data.push(nxt);
		    			}
	    			}
	    			if (self.config.animate) {
	    				self.redrawWithAnimation ();
	    			}
	    			else {
	    				self.redrawWithoutAnimation ();
	    			}
	    			var currentValue = self.group.select (".MLCurrentValue");
	    			currentValue.text (self.data.slice(-1));

	    			
	    		}, this.config.interval);
    		}
    	}
    	
    	// Stops the gauge animation (ignored if already started)
    	this.stop = function () {
    		if (this.timer != null) {
    			clearInterval (this.timer);
    			this.timer == null;
    		}
    	}
    	
    	this.redrawWithAnimation = function () {
    		// todo: reset the yscale if the max and mins differ with the new
			// data
    		var max = Math.max (this.config.max, d3.max(this.data));
    		var min = Math.min (this.config.min, d3.min (this.data));
    		if (max > this.max || min < this.min) {
    			this.resetYScale (max, min);
    		}
    		else if (max <= this.config.max || min >= this.config.min) {
    			this.resetYScale (Math.max (this.config.max,max), Math.min (this.config.min,min));
    		}
    		if (this.data.length < this.config.windowSize) {
    			this.redrawWithoutAnimation ();
    		}
    		else {
	    		this.graph.selectAll(".data")
	    			.data([this.data]) // set the new data
	    			.attr("transform", "translate(" + this.xScale(1) + ")") // set
																			// the
																			// transform
																			// to
																			// the
																			// right
																			// by
																			// xScale
																			// (1)
	    			.attr("d", this.line) // apply the new data values ... but
											// the new value is hidden at this
											// point off the right of the canvas
	    			.transition () // start the transition to bring in the new
									// value
	    			.ease("linear")
	    			.duration (this.config.transition)
	    			.style("stroke", "steelblue").style ("stroke-width", "1").style ("fill","none")
	    			.attr("transform", "translate(" + this.xScale(0) + ")"); // animate
																				// a
																				// slide
																				// to
																				// the
																				// left
																				// back
																				// to
																				// x(0)
																				// pixels
																				// to
																				// reveal
																				// the
																				// new
																				// value
	    	}
    		
    		
    	}
    	
    	this.redrawWithoutAnimation = function () {
    		this.graph.selectAll(".data")
    			.data([this.data]) // set the new data
    			.style("stroke", "steelblue").style ("stroke-width", "1").style ("fill","none")
    			.attr ("d", this.line); // apply the new data values
    	}
    	
    	// Initialization
    	this.configure (configuration);
    }
   
window.edu_cmu_rainbow_ui_display_widgets_LineGaugeComponent = function () {
	var element = this.getElement ();
	
	var gauge;
	
	this.onStateChange = function () {
		if (gauge)
			gauge.redraw (this.getState ().value);
	}
	
	this.createGauge = function (name, label, min, max, window, yLabel, threshold) {
		var config = {
				width: 100,
				height: 50,
				label: label,
				interval: 1000,
				windowSize: window,
				animate:true,
				min:min,
				max:max,
		}
		
		if (yLabel) config.yLabel = yLabel;
		if (threshold && threshold > 0) config.threshold = threshold;
		

		gauge = new MovingLineGauge(element, config);
		gauge.render ();
	}
	
	this.deactivate = function () {
		if (gauge)
			gauge.stop ();
	}
	
	this.activate = function () {
		if (gauge)
			gauge.start ();
	}
}
