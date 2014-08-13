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

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Panel;

import edu.cmu.rainbow_ui.display.ISystemViewProvider;
import edu.cmu.rainbow_ui.display.AccessibleAcmeSystemViewProvider;

/**
 * This class defines an accessible timeline view which provides access to its
 * various components for testing
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class AccessibleTimelineView extends TimelineView {

    /**
     * Create an accessible timeline view with a timeline component
     * 
     * @param systemViewProvider the system view provider that the timeline view
     *        will use
     */
    public AccessibleTimelineView(ISystemViewProvider systemViewProvider) {
        super(systemViewProvider);
    }

    /**
     * Create an accessible timeline view with a mock timeline component
     * 
     * @param systemViewProvider the mock acme system view provider that the
     *        timeline view will use
     */
    AccessibleTimelineView(AccessibleAcmeSystemViewProvider systemViewProvider) throws Exception {
        super(systemViewProvider, MockTimeline.class);
    }

    /**
     * Getter for the play button
     * 
     * @return the play button
     */
    public Button getPlayButton() {
        return super.playButton;
    }

    /**
     * Getter for the stop button
     * 
     * @return the stop button
     */
    public Button getStopButton() {
        return super.stopButton;
    }

    /**
     * Getter for the rewind button
     * 
     * @return the rewind button
     */
    public Button getRewindButton() {
        return super.rewindButton;
    }

    /**
     * Getter for the play combobox
     * 
     * @return the play combobox
     */
    public ComboBox getPlayRate() {
        return super.playRate;
    }

    /**
     * Getter for the current check box
     * 
     * @return the current check box
     */
    public CheckBox getCurrentCheckBox() {
        return super.currentCheckBox;
    }

    /**
     * Getter for the auto scroll check box
     * 
     * @return the auto scroll check box
     */
    public CheckBox getAutoScrollCheckBox() {
        return super.autoScrollCheckBox;
    }

    /**
     * Getter for the zoom in button
     * 
     * @return the zoom in button
     */
    public Button getZoomInButton() {
        return super.zoomInButton;
    }

    /**
     * Getter for the zoom out button
     * 
     * @return the zoom out button
     */
    public Button getZoomOutButton() {
        return super.zoomOutButton;
    }

    /**
     * Getter for the zoom factor constant
     * 
     * @return the zoom factor
     */
    public float getZoomFactor() {
        return super.ZOOM_FACTOR;
    }

    /**
     * Getter for the timeline panel
     * 
     * @return the timeline panel
     */
    public Panel getTimelinePanel() {
        return super.timelinePanel;
    }

    /**
     * Getter for the time display date field
     * 
     * @return the time display date field
     */
    public DateField getTimeDisplay() {
        return super.timeDisplay;
    }
}
