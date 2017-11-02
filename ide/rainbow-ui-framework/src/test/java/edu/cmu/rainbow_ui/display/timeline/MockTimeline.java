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

package edu.cmu.rainbow_ui.display.timeline;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class defines a mock timeline used for testing the timeline view
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class MockTimeline extends Timeline {
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
     * The width of the canvas
     */
    public int canvasWidth;


    public MockTimeline(TimelineView view) {
        super(view, new Date());
        isCurrent = true;
        playRate = 1;
        startDate = new Date();
        maxDate = new Date();
        idealRangeSize = 1000 * 60;// Set default range to 1 minute
        currentDate = new Date();
    }

    /**
     * Set the current date of the widget
     * 
     * @param curent the current time of the system
     */
    public void setCurrentDate(Date current) {
        currentDate = current;
        if (!this.isCurrent()) {
            super.view.setTimeDisplay(current);
        }
    }

    /**
     * Set the latest date of the system
     * 
     * @param maxDate the latest that can be displayed by the system
     */
    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    /**
     * Set the current date of the widget
     * 
     * @param currentDate, the current date of the system marked by the circle
     *        and line
     */
    public void setCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
        if (!isCurrent) {
            this.view.setTimeDisplay(startDate);
        }
    }

    /**
     * Set the ideal range for the widget
     * 
     * @param idealRange, the ideal difference between the visible start time
     *        and the visible end time
     */
    public void setIdealRange(int idealRange) {
        idealRangeSize = idealRange;
    }

    /**
     * Get the current date of the widget
     * 
     * @return the current date of the system marked by the circle and line
     */
    public Date getCurrentTime() {
        return currentDate;
    }

    /**
     * Get the earliest date in the system
     * 
     * @return the earliest date that can be displayed by the system
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Get the latest date in the system
     * 
     * @return the latest date that can be displayed by the system
     */
    public Date getMaxDate() {
        return maxDate;
    }
    
    /**
     * Set the rate at which the timeline will scroll forward or backward
     * automatically when in historical mode
     */
    public void setPlayRate(double rate) {
        playRate = rate;
    }

    /**
     * Get whether or not the system is displaying the current date
     * 
     * @return true if the system is displaying the current date
     */
    public boolean isCurrent() {
        return isCurrent;
    }

    /**
     * Gets the ideal range of the visible dates
     * 
     * @return the ideal difference between the visible start and end times
     */
    public long getIdealRangeSize() {
        return idealRangeSize;
    }
    
    /**
     * Get the width of the timeline canvas
     * 
     * @param the width of the timeline canvas in pixels
     */
    public int getCanvasWidth() {
        return canvasWidth;
    }

    public double getPlayRate() {
        return this.playRate;
    }
    
    public TimelinePlayState getPlayState() {
        return super.state;
    }
    
    public void manualIncrement(boolean direction) {
        super.increment(direction);
    }
}
