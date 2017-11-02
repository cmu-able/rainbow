/*
 * The MIT License
 *
 * Copyright 2014 CMU MSIT-SE Rainbow Team.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package edu.cmu.rainbow_ui.display.client;

import java.util.Date;

import com.vaadin.shared.AbstractComponentState;

/**
 * This class defines the Timeline state
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class TimelineState extends AbstractComponentState {
    /**
     * The earliest time the timeline can scroll to
     */
    public Date startDate;
    /**
     * The difference between the visible start and visible end
     */
    public int idealRangeSize;
    /**
     * The current time at the marker
     */
    public Date currentDate;
    /**
     * Used to determine if the marker is at current and should update
     * continuously
     */
    public boolean isCurrent;
    /**
     * The rate at which the system should play forward or rewind backward
     */
    public double playRate;
    /**
     * The latest time the timeline can scroll to
     */
    public Date maxDate;
    /**
     * Whether or not the timeline should autoscroll
     */
    public boolean autoscroll;

    /**
     * Default constructor.
     */
    public TimelineState() {
        super();
        isCurrent = true;
        playRate = 1;
        startDate = new Date();
        maxDate = new Date();
        idealRangeSize = 1000 * 60 * 15; // Set default range to 15 minutes
        currentDate = new Date();
        autoscroll = true;
    }
}
