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
package edu.cmu.rainbow_ui.storage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

/**
 * Unit tests for HistoryProvider
 * 
 * @author Anastasia Timoshenko <atimoshe@andrew.cmu.edu>
 */
public class HistoryProviderTest {

    MockDatabaseConnector databaseconn = new MockDatabaseConnector();
    HistoryProvider hp = new HistoryProvider(databaseconn);

    public HistoryProviderTest() {

    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testgetModelState() {
        IModelInstance<?> modelstate = null;
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        modelstate = hp.getModelState(date);
        assertNotNull(modelstate);

    }

    @Test
    public void testgetEventRange() {
        List<IRainbowMessage> events = null;
        Calendar cal1 = Calendar.getInstance();
        Date date1 = cal1.getTime();
        Calendar cal2 = Calendar.getInstance();
        Date date2 = cal2.getTime();
        events = hp.getEventRange(date1, date2);
        assertNotNull(events);
    }

    @Test
    public void testgetEventRangeByType() {
        List<IRainbowMessage> events = null;
        Calendar cal1 = Calendar.getInstance();
        Date date1 = cal1.getTime();
        Calendar cal2 = Calendar.getInstance();
        Date date2 = cal2.getTime();
        events = hp.getEventRangeByType("channel", date1, date2);
        assertNotNull(events);
    }

    @Test
    public void testgetModelEventRange() {
        List<IRainbowMessage> events = null;
        Calendar cal1 = Calendar.getInstance();
        Date date1 = cal1.getTime();
        Calendar cal2 = Calendar.getInstance();
        Date date2 = cal2.getTime();
        events = hp.getModelEventRange(date1, date2);
        assertNotNull(events);
    }

    @Test
    public void testgetEvent() {
        IRainbowMessage event = null;
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        event = hp.getEvent(date);
        assertNotNull(event);

    }
}
