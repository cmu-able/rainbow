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

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Line;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

import edu.cmu.rainbow_ui.common.MockRainbow;

/**
 * This class defines the Timeline client side widget
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class TimelineWidget extends Composite {

    public static final String CLASSNAME = "timeline";

    private DrawingArea timelineCanvas;
    private Date currentDate;
    private Date maxDate;
    private Date startDate;
    private Date visibleStartDate;
    private Date visibleEndDate;
    private int idealRangeSize;
    private final int NUM_VISIBLE_TICKS = 50;
    private final int TIMELINE_HEIGHT = 45;
    private PopupPanel popupPanel;
    private int currentLinePos;
    private double offset;
    private ArrayList<Boolean> tickMarks;
    private int handleWidth;
    private int scrollLoc;

    /**
     * Constructor, creates a timeline widget
     */
    public TimelineWidget() {
        super();

        timelineCanvas = new DrawingArea(1000, TIMELINE_HEIGHT);
        tickMarks = new ArrayList<Boolean>();
        for (int i = 0; i < NUM_VISIBLE_TICKS; i++) {
            tickMarks.add(false);
            if (i % 4 == 0) {
                tickMarks.set(i, true);
            }
        }

        offset = 0;

        /* Set the composition root */
        initWidget(timelineCanvas);
        setStyleName(CLASSNAME);
    }

    /**
     * Draws the timeline which consists of the timeline area, the tick marks,
     * the current time marker and the scrollbar
     */
    public void drawTimeline() {
        int increment = (int) (idealRangeSize / (NUM_VISIBLE_TICKS * 1.0f));

        timelineCanvas.clear();
        Rectangle backgroundRectangle = new Rectangle(0, 0,
                timelineCanvas.getWidth(), timelineCanvas.getHeight());
        backgroundRectangle.setStrokeColor("#ffffff");
        timelineCanvas.add(backgroundRectangle);

        Date timeShown = visibleStartDate;
        for (int i = 0; i < NUM_VISIBLE_TICKS; i++) {
            int length = 12;
            boolean addText = false;
            if (tickMarks.get(i)) {
                length = 25;
                addText = true;
            }

            Line tick = new Line((int) (i * 20 + offset), 0,
                    (int) (i * 20 + offset), length);
            timelineCanvas.add(tick);

            if (addText) {
                Text text = new Text((int) (i * 20 + offset), 35,
                        DateTimeFormat.getFormat("HH:mm:ss.SSS").format(
                                timeShown));
                text.setFontSize(10);
                timelineCanvas.add(text);
            }

            timeShown = new Date(timeShown.getTime() + increment);
            if (timeShown.getTime() > visibleEndDate.getTime()) {
                break;
            }
        }

        Line currentLine = new Line((int) (currentLinePos + offset), 0,
                (int) (currentLinePos + offset), 25);
        timelineCanvas.add(currentLine);
        Rectangle fillRectangle = new Rectangle(0, 0,
                (int) (currentLinePos + offset), 25);
        fillRectangle.setFillColor("#33CCFF");
        fillRectangle.setFillOpacity(0.5);
        timelineCanvas.add(fillRectangle);

        // Draw scrollbar
        Rectangle scrollBackground = new Rectangle(0, 35,
                timelineCanvas.getWidth(), 10);
        scrollBackground.setFillColor("#d1d1ff");
        timelineCanvas.add(scrollBackground);

        handleWidth = timelineCanvas.getWidth();
        int range = (int) (visibleEndDate.getTime() - visibleStartDate
                .getTime());

        if (startDate.getTime() != maxDate.getTime()) {
            double percentage = range
                    / (1.0f * (maxDate.getTime() - startDate.getTime()));
            handleWidth = (int) (percentage * timelineCanvas.getWidth());
            if(handleWidth < 20) {
                handleWidth = 20;
            }
        }

        scrollLoc = 0;
        if (maxDate.getTime() != startDate.getTime()) {
            if((maxDate.getTime() - startDate.getTime()) > range) {
                long center = (long) (range / 2.0f + visibleStartDate.getTime());
                scrollLoc = (int) ((center - startDate.getTime()) / (1.0f * (maxDate
                        .getTime() - startDate.getTime()))
                        * (timelineCanvas.getWidth()));
            } else {
                scrollLoc = 0;
            }
        }

        Logger.getLogger(TimelineWidget.class.getName()).log(Level.INFO,
                "LOC: " + scrollLoc + "  WIDTH: " + handleWidth);

        Rectangle scrollHandle = new Rectangle(scrollLoc, 35, handleWidth, 10);
        scrollHandle.setFillColor("#29a3cc");
        timelineCanvas.add(scrollHandle);
    }

    /**
     * Set the current date of the widget
     * 
     * @param currentDate, the current date of the system marked by the circle
     *        and line
     */
    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
        setCurrentLinePos();
    }

    /**
     * Set the max time of the widget
     * 
     * @param maxTime, the latest date that can be displayed
     */
    public void setMaxDate(Date maxTime) {
        this.maxDate = maxTime;
    }

    /**
     * Set the start time of the widget
     * 
     * @param maxTime, the earliest date that can be displayed
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Set the visible start time of the widget
     * 
     * @param visibleEndDate, the earliest date that will be displayed
     */
    public void setVisibleStartDate(Date visibleStartDate) {
        this.visibleStartDate = visibleStartDate;
    }

    /**
     * Set the visible end time of the widget
     * 
     * @param maxTime, the latest date that will displayed
     */
    public void setVisibleEndDate(Date visibleEndDate) {
        this.visibleEndDate = visibleEndDate;
    }

    /**
     * Get the drawing area for the widget
     * 
     * @return the area where the widget is drawn
     */
    public DrawingArea getTimelineCanvas() {
        return this.timelineCanvas;
    }

    /**
     * Get the current date of the widget
     * 
     * @return the current date of the widget
     */
    public Date getCurrentDate() {
        return this.currentDate;
    }

    /**
     * Set the ideal visible range size
     * 
     * @param a long representing the ideal range size in milliseconds
     */
    public void setIdealRangeSize(int idealRangeSize) {
        this.idealRangeSize = idealRangeSize;
        // Handle constructor special case when current date is not yet set
        if (currentDate != null) {
            setCurrentLinePos();
        }
    }

    /**
     * Get the current width of the canvas
     * 
     * @return the current width of the canvas
     */
    public int getCanvasWidth() {
        return this.timelineCanvas.getWidth();
    }

    /**
     * Set the current time location to the given x position
     * 
     * @param x the x location where the current time is
     */
    public void setCurrentLoc(int x) {
        double loc = x / 20.0;
        int increment = (int) (idealRangeSize / NUM_VISIBLE_TICKS);
        this.setCurrentDate(new Date((long) (loc * increment + this.startDate
                .getTime())));
        setCurrentLinePos();
    }

    /**
     * Get the date at a current x position
     * 
     * @param x the x location where the date is requested
     * @return the date at that location
     */
    public Date getCurrentLocDate(int x) {
        double loc = x / 20.0;
        int increment = (int) (idealRangeSize / NUM_VISIBLE_TICKS);
        return new Date((long) (loc * increment + this.startDate.getTime()));
    }

    /**
     * Create a tooltip for displaying the current time
     */
    public void createCurrentLocDateToolTip() {
        if (popupPanel != null) {
            popupPanel.removeFromParent();
        }
        popupPanel = new PopupPanel();
    }

    /**
     * Show a tooltip at the given location
     * 
     * @param x the x location that represents the location where the time is
     * @param xLoc the x location to show the tooltip relative to the browser
     *        window
     * @param yLoc the y location to show the tooltip relative to the browser
     *        window
     */
    public void showCurrentLocDateToolTip(int x, int xLoc, int yLoc) {
        Date currentLocDate = getCurrentLocDate(x);
        if (currentLocDate.getTime() < maxDate.getTime()) {
            HTML contents = new HTML(currentLocDate.toString());
            popupPanel.clear();
            popupPanel.add(contents);
            popupPanel.setPopupPosition(xLoc, yLoc);
            popupPanel.show();
            popupPanel.addStyleName("currentTimeLoc.popup");
        } else {
            hideCurrentLocDateToolTip();
        }
    }

    /**
     * Hides the current time tool tip if it is show, if it is not shown nothing
     * will happen
     */
    public void hideCurrentLocDateToolTip() {
        if (popupPanel != null) {
            popupPanel.removeFromParent();
        }
    }

    /**
     * Get the ideal range size for the timeline
     * 
     * @return the ideal range size in milliseconds
     */
    public int getIdealRangeSize() {
        return this.idealRangeSize;
    }

    /**
     * Sets the current position for the the highlight rectangle
     */
    private void setCurrentLinePos() {
        int increment = (int) (idealRangeSize / NUM_VISIBLE_TICKS);
        double percentage = ((currentDate.getTime() - visibleStartDate
                .getTime()) / ((visibleEndDate.getTime() - visibleStartDate
                .getTime()) * 1.0f));
        int numTicks = (int) ((visibleEndDate.getTime() - visibleStartDate
                .getTime()) / increment);
        int width = (20 * numTicks) + 80;
        currentLinePos = (int) (percentage * (width - 80));
    }

    /**
     * Sets the offset for drawing the timeline
     * 
     * @param offset, the offset from the left edge of the time line that the
     * tick marks will be drawn in pixels
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }

    /**
     * Gets the offset for drawing the timeline
     * 
     * @return the offset from the left edge of the time line that the
     * tick marks will be drawn in pixels
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Gets the visible start date
     * 
     * @return the earliest date currently visible on the timeline
     */
    public Date getVisibleStartDate() {
        return this.visibleStartDate;
    }

    /**
     * Gets the visible end date
     * 
     * @return the latest date currently visible on the timeline
     */
    public Date getVisibleEndDate() {
        return this.visibleEndDate;
    }

    /**
     * Gets the number of tick marks on the timeline
     * 
     * @return the number of tick marks drawn on the timeline
     */
    public int getNumTicks() {
        return this.NUM_VISIBLE_TICKS;
    }

    /**
     * Gets the arraylist of tick marks
     * 
     * @return the arraylist of tick marks where true represents a tick mark
     * that will have a time annotation
     */
    public ArrayList<Boolean> getTickMarks() {
        return this.tickMarks;
    }
    
    /**
     * Called to determine whether or not the scroll bar was pressed
     * 
     * @param x, the location where the mouse was pressed relative to this element
     * @return a boolean that is true if it is within the x range of the scroll
     * bar and false otherwise
     */
    public boolean scrollPressed(int x) {
        if (x > scrollLoc && x < scrollLoc + handleWidth) {
            return true;
        }
        return false;
    }

    /**
     * Scrolls the timeline by the provided amount
     * 
     * @param amount, the amount in pixels that the scroll bar was moved
     */
    public void scroll(int amount) {
        //TODO: Implement Scrolling
    }
}
