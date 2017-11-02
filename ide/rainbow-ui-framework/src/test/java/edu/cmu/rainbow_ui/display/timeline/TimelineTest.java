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

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.util.Calendar;

import edu.cmu.rainbow_ui.common.SystemConfiguration;
import edu.cmu.rainbow_ui.display.MockAcmeRuntimeAggregator;
import edu.cmu.rainbow_ui.display.AccessibleAcmeSystemViewProvider;
import edu.cmu.rainbow_ui.display.ui.DummyTestUI;
import edu.cmu.rainbow_ui.storage.MockDatabaseConnector;

/**
 * This class defines unit tests for the timeline server side component
 * 
 * @author Zachary Sweigart <zsweigar@andrew.cmu.edu>
 */
public class TimelineTest {
    private final static String SYSTEM_CONFIG_FILE = "src/test/resources/system_test.properties";
    private MockTimelineView timelineView;
    private MockTimeline timeline;
    private AccessibleAcmeSystemViewProvider svp;

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        SystemConfiguration sysConfig = new SystemConfiguration(
                SYSTEM_CONFIG_FILE);
        svp = new AccessibleAcmeSystemViewProvider(new MockAcmeRuntimeAggregator(
                sysConfig, new MockDatabaseConnector()), new DummyTestUI(),
                sysConfig);
        timelineView = new MockTimelineView(svp);
        timeline = (MockTimeline) timelineView.getTimeline();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void timelineAutoPlay() {        
        timeline.setPlayRate(1.0);
        assertEquals(1, (int)timeline.getPlayRate());
        
        timeline.stop();
        assertEquals(TimelinePlayState.STOPPED, timeline.getPlayState());
        
        timeline.play();
        assertEquals(TimelinePlayState.PLAY, timeline.getPlayState());
        
        timeline.rewind();
        assertEquals(TimelinePlayState.REWIND, timeline.getPlayState());
        
        timeline.stop();
        assertEquals(TimelinePlayState.STOPPED, timeline.getPlayState());
    }

    @Test
    public void timelineIncrement() {
        timeline.setPlayRate(1.0);
        assertEquals(1, (int)timeline.getPlayRate());
        
        timeline.stop();
        assertEquals(TimelinePlayState.STOPPED, timeline.getPlayState());
        
        timeline.setCurrent(false);
        assertEquals(false, timeline.isCurrent());
        
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        timeline.setCurrentDate(date);
        assertEquals(date, timeline.getCurrentTime());
        
        timeline.manualIncrement(false);
        calendar.add(Calendar.MILLISECOND, -500);
        assertEquals(calendar.getTime(), timeline.getCurrentTime());     

        timeline.manualIncrement(true);
        calendar.add(Calendar.MILLISECOND, 500);
        assertEquals(date, timeline.getCurrentTime());
        
        timeline.manualIncrement(true);
        calendar.add(Calendar.MILLISECOND, 500);
        assertEquals(calendar.getTime(), timeline.getCurrentTime());
    }
}
