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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;

import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.MockAcmeRuntimeAggregator;
import edu.cmu.rainbow_ui.display.MockAcmeSystemViewProvider;
import edu.cmu.rainbow_ui.display.ui.DummyTestUI;
import edu.cmu.rainbow_ui.storage.MockDatabaseConnector;
import org.junit.Ignore;

/**
 * This class defines unit tests for the timeline view
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class TimelineViewTest {
    private final static String SYSTEM_CONFIG_FILE = "src/test/resources/system_test.properties";
    private AccessibleTimelineView timelineView;
    private MockTimeline timeline;
    private MockAcmeSystemViewProvider svp;

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        SystemConfiguration sysConfig = new SystemConfiguration(
                SYSTEM_CONFIG_FILE);
        svp = new MockAcmeSystemViewProvider(new MockAcmeRuntimeAggregator(
                sysConfig, new MockDatabaseConnector()), new DummyTestUI(),
                sysConfig);
        timelineView = new AccessibleTimelineView(svp);
        timeline = (MockTimeline) timelineView.getTimeline();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void timelineViewActivation() {
        timelineView.activate();
        assertEquals(true, timelineView.isActive());

        timelineView.deactivate();
        assertEquals(false, timelineView.isActive());
    }

    @Ignore
    @Test
    public void timelineViewUpdate() throws InterruptedException {
        /**
         * TODO: test relies on event passing, which makes it unpredictable.
         * 
         * setCurrent -> setValue ->(event) valueChange -> timeline.setCurrent
         */
        timelineView.setCurrent(true);
        assertEquals(true, timelineView.isCurrent());
        assertEquals(true, timeline.isCurrent());
        timelineView.update();
        assertEquals(timeline.getMaxDate(), timeline.getCurrentTime());

        timelineView.setCurrent(false);
        timelineView.setTimeDisplay(new Date());
        Thread.sleep(1000);
        assertEquals(false, timelineView.isCurrent());
        assertEquals(false, timeline.isCurrent());
        Date currentDate = timeline.getCurrentTime();
        timelineView.update();
        assertNotEquals(timeline.getMaxDate(), timeline.getCurrentTime());
        assertEquals(currentDate, timeline.getCurrentTime());
    }

    @Test
    public void timelineViewZoom() {
        long idealRange = timeline.getIdealRangeSize();
        float zoomFactor = timelineView.getZoomFactor();
        Button zoomIn = timelineView.getZoomInButton();
        zoomIn.click();
        assertEquals((int) (idealRange / zoomFactor),
                (int) (timeline.getIdealRangeSize()));
        Button zoomOut = timelineView.getZoomOutButton();
        zoomOut.click();
        assertEquals(idealRange, timeline.getIdealRangeSize());
        zoomOut.click();
        assertEquals((int) (idealRange * zoomFactor),
                (int) (timeline.getIdealRangeSize()));
    }

    @Test
    public void currentHistorical() {
        CheckBox currentCheckBox = timelineView.getCurrentCheckBox();
        DateField timeDisplay = timelineView.getTimeDisplay();
        CheckBox autoScrollCheckBox = timelineView.getAutoScrollCheckBox();
        
        currentCheckBox.setValue(true);
        assertEquals(true, currentCheckBox.getValue());
        assertEquals(true, timeline.isCurrent());
        assertEquals(true, svp.isCurrent());
        assertEquals(false, timeDisplay.isEnabled());
        assertEquals(true, autoScrollCheckBox.isEnabled());
        
        currentCheckBox.setValue(false);
        assertEquals(false, currentCheckBox.getValue());
        assertEquals(false, timeline.isCurrent());
        assertEquals(false, svp.isCurrent());
        assertEquals(true, timeDisplay.isEnabled());
        assertEquals(false, autoScrollCheckBox.isEnabled());
    }
    
    @Test
    public void timelineViewAutoScroll() {
        CheckBox autoScrollCheckBox = timelineView.getAutoScrollCheckBox();
        timelineView.enableAutoScroll();
        assertEquals(true, autoScrollCheckBox.isEnabled());

        timelineView.disableAutoScroll();
        assertEquals(false, autoScrollCheckBox.isEnabled());
    }
    
    @Test
    public void timelineViewTimeDisplay() {
        DateField timeDisplay = timelineView.getTimeDisplay();
        Date date = new Date();
        timelineView.setTimeDisplay(date);
        assertEquals(date, timeDisplay.getValue());
    }
    
    @Test
    public void timelineViewAutoPlay() {
        Button playButton = timelineView.getPlayButton();
        Button stopButton = timelineView.getStopButton();
        Button rewindButton = timelineView.getRewindButton();
        ComboBox playRate = timelineView.getPlayRate();
        
        playRate.setValue("2.0x");
        assertEquals(2, (int)(timeline.getPlayRate()));
    }
}
