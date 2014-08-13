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

import com.vaadin.ui.AbstractComponent;

import edu.cmu.rainbow_ui.display.client.TimelineClientRpc;
import edu.cmu.rainbow_ui.display.client.TimelineConnector;
import edu.cmu.rainbow_ui.display.client.TimelineServerRpc;
import edu.cmu.rainbow_ui.display.client.TimelineState;
import edu.cmu.rainbow_ui.display.client.TimelineWidget;

/**
 * This class defines the Timeline server side component
 * 
 * <p>
 * The Timeline
 * </p>
 * <p>
 * This is a server-side part of a custom Vaadin widget. It maintains
 * server-side functions and state and communicates it to client-side GWT
 * implementation via shared state and connectors.
 * 
 * @see TimelineWidget
 * @see TimelineState
 * @see TimelineConnector </p>
 *      <p>
 *      Server-side component is able to communicate both ways with the
 *      client-side widget via RPC connection.
 * @see TimelineServerRpc
 * @see TimelineClientRpc </p>
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class Timeline extends AbstractComponent {
    protected float zoomFactor;
    protected TimelinePlayState state;
    protected Timer timer;
    protected TimerTask task;
    protected TimelineView view;

    /**
     * Create a timeline server rpc so that the client side widget can alter the
     * server side state
     */
    private TimelineServerRpc rpc = new TimelineServerRpc() {

        @Override
        public void setIsCurrent() {
            Timeline.this.getState().isCurrent = true;
            Timeline.this.view.setCurrent(true);
            Timeline.this.view.enableAutoScroll();
        }

        @Override
        public void setHistorical(Date current) {
            Timeline.this.getState().isCurrent = false;
            Timeline.this.getState().currentDate = current;
            Timeline.this.view.setCurrent(false);
            Timeline.this.view.disableAutoScroll();
            Timeline.this.view.setHistoricalDate(current);
        }

        @Override
        public void setCurrentDate(Date current) {
            Timeline.this.getState().currentDate = current;
        }

    };

    /**
     * Timeline server side component constructor
     * 
     * @param view a timeline view that will hold the timeline
     */
    public Timeline(TimelineView view, Date startDate) {
        this.view = view;
        state = TimelinePlayState.STOPPED;
        timer = new Timer();
        registerRpc(rpc);
        getState().startDate = startDate;
    }

    @Override
    protected TimelineState getState() {
        return (TimelineState) super.getState();
    }

    /**
     * Set the current date of the widget
     * 
     * @param curent the current time of the system
     */
    public void setCurrentDate(Date current) {
        getState().currentDate = current;
        if (!this.isCurrent()) {
            this.view.setTimeDisplay(current);
        }
    }
    /**
     * Set the earliest date of the system
     * 
     * @param startDate the earliest that can be displayed by the system
     */
    public void setStartDate(Date startDate) {
        getState().startDate = startDate;
        markAsDirty();
    }
    
    /**
     * Set the latest date of the system
     * 
     * @param maxDate the latest that can be displayed by the system
     */
    public void setMaxDate(Date maxDate) {
        getState().maxDate = maxDate;
        markAsDirty();
    }

    /**
     * Set the current date of the widget
     * 
     * @param currentDate, the current date of the system marked by the circle
     *        and line
     */
    public void setCurrent(boolean isCurrent) {
        getState().isCurrent = isCurrent;
    }

    /**
     * Set the ideal range for the widget
     * 
     * @param idealRange, the ideal difference between the visible start time
     *        and the visible end time
     */
    public void setIdealRange(int idealRange) {
        getState().idealRangeSize = idealRange;
    }

    /**
     * Get the current date of the widget
     * 
     * @return the current date of the system marked by the circle and line
     */
    public Date getCurrentTime() {
        return getState().currentDate;
    }

    /**
     * Get the earliest date in the system
     * 
     * @return the earliest date that can be displayed by the system
     */
    public Date getStartDate() {
        return getState().startDate;
    }

    /**
     * Get the latest date in the system
     * 
     * @return the latest date that can be displayed by the system
     */
    public Date getMaxDate() {
        return getState().maxDate;
    }

    /**
     * Stop the timeline from automatically moving when in historical mode
     */
    public void stop() {
        state = TimelinePlayState.STOPPED;
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Scroll the timeline forward when in historical mode
     */
    public void play() {
        state = TimelinePlayState.PLAY;
        if (task != null) {
            task.cancel();
        }
        task = new TimerTask() {

            @Override
            public void run() {
                increment(true);
                if(getCurrentTime().after(getStartDate())) {
                    setCurrentDate(getMaxDate());
                    this.cancel();
                    view.disableStop();
                }
            }
        };
        if(!getCurrentTime().after(getMaxDate())) {
            timer = new Timer();
            timer.schedule(task, 0, 500);
        } else {
            setCurrentDate(getMaxDate());
        }
    }

    /**
     * Scroll the timeline backward when in historical mode
     */
    public void rewind() {
        state = TimelinePlayState.REWIND;
        if (task != null) {
            task.cancel();
        }
        task = new TimerTask() {

            @Override
            public void run() {
                increment(false);
                if(getCurrentTime().before(getStartDate())) {
                    setCurrentDate(getStartDate());
                    this.cancel();
                    view.disableStop();
                }
            }
        };
        if(!getCurrentTime().before(getStartDate())) {
            timer = new Timer();
            timer.schedule(task, 0, 500);
        } else {
            setCurrentDate(getMaxDate());
        }
    }

    /**
     * Set the rate at which the timeline will scroll forward or backward
     * automatically when in historical mode
     */
    public void setPlayRate(double rate) {
        getState().playRate = rate;
    }

    /**
     * Get whether or not the system is displaying the current date
     * 
     * @return true if the system is displaying the current date
     */
    public boolean isCurrent() {
        return getState().isCurrent;
    }

    /**
     * Gets the ideal range of the visible dates
     * 
     * @return the ideal difference between the visible start and end times
     */
    public long getIdealRangeSize() {
        return getState().idealRangeSize;
    }

    /**
     * Scrolls forward or backward automatically
     * 
     * @param direction, true for scrolling forward, false for scrolling backward
     */
    protected void increment(boolean direction) {
        int factor = 1;
        if (!direction) {
            factor = -1;
        }
        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance();
        calendar.setTime(Timeline.this.getCurrentTime());
        calendar.add(Calendar.MILLISECOND,
                (int) (factor * 500 * getState().playRate));
        setCurrentDate(calendar.getTime());
        view.setTimeDisplay(getCurrentTime());
    }

    public void enableAutoScroll() {
        getState().autoscroll = true;
    }
    
    public void disableAutoScroll() {
        getState().autoscroll = false;
    }
}
